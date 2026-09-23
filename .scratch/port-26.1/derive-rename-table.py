import re,json,collections
PRIM={'void':'V','boolean':'Z','byte':'B','char':'C','short':'S','int':'I','long':'J','float':'F','double':'D'}
def load_pg(path):
    classes={}  # named -> (obf, members list)
    cur=None
    for line in open(path):
        if line.startswith('#'): continue
        if not line.startswith(' '):
            m=re.match(r'(\S+) -> (\S+):',line); cur=m.group(1); classes[cur]={'obf':m.group(2),'m':[],'f':[]}
        else:
            s=line.strip()
            m=re.match(r'(?:\d+:\d+:)?(\S+) (\S+)\((.*)\)(?::\d+:\d+)? -> (\S+)',s)
            if m: classes[cur]['m'].append((m.group(1),m.group(2),m.group(3),m.group(4))); continue
            m=re.match(r'(\S+) (\S+) -> (\S+)',s)
            if m: classes[cur]['f'].append((m.group(1),m.group(2),m.group(3)))
    n2o={k:v['obf'] for k,v in classes.items()}
    def t(ty):
        dims=0
        while ty.endswith('[]'): ty=ty[:-2]; dims+=1
        d=PRIM.get(ty) or 'L'+n2o.get(ty,ty).replace('.','/')+';'
        return '['*dims+d
    meth={}; fld={}; cls={}
    for k,v in classes.items():
        cls[v['obf']]=k
        for ret,name,args,obf in v['m']:
            desc='('+''.join(t(a) for a in args.split(',') if a)+')'+t(ret)
            meth[(v['obf'],obf,desc)]=(k,name,args,ret)
        for ty,name,obf in v['f']:
            fld[(v['obf'],obf)]=(k,name,ty)
    return cls,meth,fld
def load_tiny(path):
    cls={};meth={};fld={}
    for line in open(path):
        p=line.rstrip('\n').split('\t')
        if p[0]=='CLASS': cls[p[1]]=p[2]
        elif p[0]=='METHOD': meth[(p[1],p[3],p[2])]=p[4]
        elif p[0]=='FIELD': fld[(p[1],p[3])]=p[4]
    return cls,meth,fld
def named_by_int(pg,tiny):
    pc,pm,pf=load_pg(pg); tc,tm,tf=load_tiny(tiny)
    C={};M={};F={}
    for obf,named in pc.items():
        i=tc.get(obf.replace('.','/'))
        if i: C[i]=named
    for (o,n,d),v in pm.items():
        i=tm.get((o.replace('.','/'),n,d))
        if i: M.setdefault(i,set()).add(v)
    for (o,n),v in pf.items():
        i=tf.get((o.replace('.','/'),n))
        if i: F[i]=v
    return C,M,F
a=named_by_int('client-1.21.1.txt','int-1.21.1.tiny')
b=named_by_int('client-1.21.11.txt','int-1.21.11.tiny')
out={'class':{},'method':[],'field':[]}
for i,n in a[0].items():
    if i in b[0] and b[0][i]!=n: out['class'][n]=b[0][i]
removed=[n for i,n in a[0].items() if i not in b[0]]
mr=0
for i,vs in a[1].items():
    if i in b[1]:
        for (own,name,args,ret) in vs:
            for (own2,name2,args2,ret2) in b[1][i]:
                if name!=name2 or own!=own2: out['method'].append([own,name,args,own2,name2,args2]); break
for i,v in a[2].items():
    if i in b[2] and (b[2][i][1]!=v[1] or b[2][i][0]!=v[0]): out['field'].append([v[0],v[1],b[2][i][0],b[2][i][1]])
json.dump(out,open('rename-1.21.1-to-1.21.11.json','w'),indent=0)
print('classes renamed/moved',len(out['class']),'classes removed(no int match)',len(removed))
print('methods renamed',len(out['method']),'fields renamed',len(out['field']))
for k in ['net.minecraft.resources.ResourceLocation','net.minecraft.client.gui.GuiGraphics','net.minecraft.util.FastColor','net.minecraft.advancements.critereon.ItemPredicate','net.minecraft.Util','net.minecraft.advancements.critereon.InventoryChangeTrigger']:
    print(k,'->',out['class'].get(k, 'REMOVED' if k in removed else 'same'))
