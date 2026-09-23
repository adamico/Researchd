import re,collections,json,sys
ROOT='/Users/kc00l/minecraft_mods/researchd-src/'
lines=open('errs.log').read().split('\n')
errs=[];i=0
while i<len(lines):
    m=re.match(r'^(src/\S+\.java):(\d+): error: (.*)$',lines[i])
    if m:
        e={'file':m.group(1),'line':int(m.group(2)),'msg':m.group(3),'body':[]}
        j=i+1
        while j<len(lines) and not re.match(r'^src/\S+\.java:\d+: (error|warning)',lines[j]) and not re.match(r'^\d+ (errors?|warnings?)',lines[j]):
            e['body'].append(lines[j]); j+=1
        errs.append(e); i=j
    else: i+=1
def field(e,k):
    for b in e['body']:
        s=b.strip()
        if s.startswith(k+':'): return s[len(k)+1:].strip()
    return ''
def ovr(e):
    src=open(ROOT+e['file']).read().split('\n'); n=e['line']
    for k in range(n-1,min(n+4,len(src))):
        mm=re.search(r'(\w+)\s*\(',src[k])
        if mm and '@Override' not in src[k]: return mm.group(1)
    return '?'
INPUT={'mouseClicked','mouseDragged','mouseReleased','keyPressed','charTyped','keyReleased','onClick','onDrag','onRelease','clicked','onPress','mouseScrolled'}
GUIREN={'render','renderWidget','renderBackground','renderBlurredBackground','renderLabels','renderBg'}
COMPAT=r'KubeJS|JEICompat|EMICompat|FTBTeams|immersiveengineering|UnlockIEMultiblock|compat\.'
PDL=r'portingdeadlibs|GuiUtils|RGBAColor|HandlerUtils|PDLDeferredRegister|ContainerBlockEntity|SidedEnergyStorage|IOAction|\bItemHandler\b|\bEnergyStorage\b|getItemHandler|getEnergyStorage|updateData|EnergyBarWidget'
NEO=r'sendToServer|ExistingFileHelper|model\.generators|BlockStateProvider|BlockModelBuilder|models\(\)|modLoc|mcLoc|includeServer|includeClient|getExistingFileHelper|RenderTypeHelper|ModelData|model\.data|variable dist|onDestroyedByPlayer|EnergyHandler|IEnergyStorage|registerBlockEntityRenderer|RegisterAdditional|FMLEnvironment|KeyMapping\('
RENDER=r'RenderType|RenderStateShard|BlockRenderDispatcher|BakedModel|ItemBlockRenderTypes|enableBlend|disableBlend|enableDepthTest|disableDepthTest|setShaderColor|getBlockRenderer|getItemRenderer|PlayerFaceRenderer|NO_CULL|COLOR_DEPTH_WRITE|ITEM_ENTITY_TARGET|NO_TRANSPARENCY|VIEW_OFFSET|RENDERTYPE_LINES|ENTITYBLOCK_ANIMATED|getTexture|getTextureAtlas|ModelResourceLocation|getRenderBoundingBox'
B=collections.OrderedDict()
def put(k,e): B.setdefault(k,[]).append(e)
for e in errs:
    msg=e['msg']; sym=field(e,'symbol'); loc=field(e,'location'); src=e['body'][0] if e['body'] else ''
    blob=' '.join([msg,sym,loc,src])
    if re.search(COMPAT,msg+' '+sym): put('G compat sources excluded from this javac run',e); continue
    if msg.startswith('method does not override'):
        n=ovr(e); e['ovr']=n
        if n in GUIREN: put('C GUI render->extract method renames (overrides)',e)
        elif n in INPUT: put('D input-event signature change (MouseButtonEvent/KeyEvent/CharacterEvent)',e)
        elif n in ('save','saveData','loadData'): put('J2 serialization API (ValueInput/ValueOutput, SavedData)',e)
        else: put('H NeoForge/vanilla override signature changes (misc)',e)
        continue
    if re.search(r'\b(mouseClicked|mouseDragged|mouseReleased|keyPressed|charTyped|keyReleased|onDrag|onRelease|onClick)\b',msg) or 'MouseButtonInfo' in msg or 'SelectionContainerWidget is not abstract' in msg:
        put('D input-event signature change (MouseButtonEvent/KeyEvent/CharacterEvent)',e); continue
    if 'ResourceLocation' in sym or 'ResourceLocation' in msg and 'Model' not in msg:
        put('A ResourceLocation -> Identifier',e); continue
    if sym=='method location()': put('A2 ResourceKey#location() -> identifier()',e); continue
    if sym=='class GuiGraphics': put('B GuiGraphics -> GuiGraphicsExtractor (type)',e); continue
    if re.search(PDL,blob): put('F Porting-Dead-Libs API changes',e); continue
    if re.search(r'FastColor|critereon|ItemPredicate|InventoryChangeTrigger|class Util\b|variable Util\b|getAllKeys|serverLevel|getItems\(\)',blob):
        put('E vanilla 1:1 class/package/method moves derivable from mappings',e); continue
    if re.search(RENDER,blob): put('I rendering pipeline rewrites',e); continue
    if re.search(NEO,blob): put('H NeoForge API changes (datagen, networking, capabilities/transfer, FML)',e); continue
    if re.search(r'Codec|serialize|saveData|loadData|CompoundTag|Provider|SavedData|ValueInput|ValueOutput',blob):
        put('J2 serialization API (ValueInput/ValueOutput, SavedData)',e); continue
    if re.search(r'getResultItem|getIngredients|getRecipeManager|getRecipeIds|shaped\(|RecipeOutput|buildRecipes|Recipe',blob):
        put('J3 recipe API (RecipeHolder/ResourceKey<Recipe>, Ingredient, RecipeProvider)',e); continue
    if re.search(r'ClickEvent|HoverEvent',blob): put('J4 ClickEvent/HoverEvent now records/sealed',e); continue
    if re.search(r'isClientSide|has private access|final variable',msg+src): put('J5 access changes (fields -> accessors)',e); continue
    put('J vanilla misc semantic changes',e)
tot=0
for k in sorted(B): print(len(B[k]),k); tot+=len(B[k])
print('total',tot)
json.dump({k:[(x['file'],x['line'],x['msg'],field(x,'symbol'),x.get('ovr','')) for x in v] for k,v in B.items()},open('buckets.json','w'),indent=0)
