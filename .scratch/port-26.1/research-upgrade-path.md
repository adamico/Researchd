# Upgrade path 1.21.1 → 26.1.2: tool-assisted options

## Short answer

- **No turnkey tool exists** for porting a NeoForge mod from Mojang names in one version to Mojang names in another. The NeoForge 26.1 and 21.11 release posts mention no migration tooling. They suggest find-and-replace and comparing against a working old workspace ([26.1 post](https://neoforged.net/news/26.1release/) sections "Finding out what needs to be migrated" and "Other resources"; [21.11 post](https://neoforged.net/news/21.11release/) "Renaming of ResourceLocation to Identifier").
- **The primers are prose only.** Each version folder in `neoforged/.github/primers/*` holds only an `index.md`. They do contain about 1,970 `` `old` -> `new` `` bullets (for example 508 in 1.21.11 and 519 in 26.1), but these are nested and often marked "not one-to-one" (`gh api repos/neoforged/.github/contents/primers/<v>`).
- **Fabric's Loom `migrateMappings`** runs Mercury with a mapping set built by joining source and target names **on intermediary** (`fabric-loom` `MigrateSourceCodeMappingsService.java` L101-124). Its docs cover only switching mapping sets within the same MC version ([docs](https://github.com/FabricMC/fabric-docs/blob/main/versions/1.21.11/develop/porting/mappings/loom.md)). Intermediary stops at 1.21.11 ([FabricMC/intermediary/mappings](https://github.com/FabricMC/intermediary/tree/master/mappings)), and 26.1.2 publishes no mappings (piston-meta lists only `client`/`server`).
- **IntelliJ "Migrate Packages and Classes"** maps (XML) handle classes and packages only ([JetBrains](https://www.jetbrains.com/help/idea/migrate.html)). Fabric ships one for Fabric API 26.1, not for vanilla ([fabric-api-26-1-migration-map.xml](https://github.com/FabricMC/fabric-docs/blob/main/public/assets/develop/porting/fabric-api-26-1-migration-map.xml)).
- **OpenRewrite** has the generic `ChangeType` and `ChangeMethodName` (with `matchOverrides`) recipes ([docs](https://docs.openrewrite.org/recipes/java/changemethodname)), but no Minecraft or NeoForge recipe set. The only community one I found is for Bukkit InvUI ([TimSchoenle/rewrite-recipes](https://github.com/TimSchoenle/rewrite-recipes)).
- **MinecraftDev (IntelliJ plugin):** its changelog has no source-migration feature (`minecraft-dev/MinecraftDev/changelog.md`).
- **`reqsery/mc-mod-porter`** is an 8-star beta driven by a knowledge base, and "doesn't do logic changes" (its README). It's not trusted, so I didn't run it.
- **PDL itself** was ported with a plain `str.replace("ResourceLocation","Identifier")` script (`Porting-Dead-Libs@1.1.15:replace_rl_id.py`).

**I derived a real rename table** by joining Mojang 1.21.1 and 1.21.11 `client.txt` on Fabric intermediary: **714 class renames or moves and 2,942 method renames** → [`rename-1.21.1-to-1.21.11.json`](rename-1.21.1-to-1.21.11.json) (script: [`derive-rename-table.py`](derive-rename-table.py)). Spot checks are correct: `ResourceLocation→Identifier`, `ResourceKey#location→identifier`, `critereon→criterion`, `Util→util.Util`, `ServerPlayer#serverLevel→level`, `Ingredient#getItems→items` (return type changed), `CompoundTag#getAllKeys→keySet`. `FastColor` has no match because it was removed; the primer maps it to `ARGB` (1.21.2 primer L2862). For 1.21.11→26.1 no intermediary exists, so the renames come from the primer. The main ones are `GuiGraphics→GuiGraphicsExtractor` and `render*/draw*→extract*`, `drawString→text` (26.1 primer L2298-2302).

**Recommendation:** go straight to 26.1 in one hop, and don't step through intermediate versions. PDL has no builds between 1.21.1 and 26.1 (tag 1.1.8 targets 1.21.1 and 1.1.9+ target 26.1; `gradle.properties` per tag). Run one scripted rename pass (the derived table plus a small hand-written 26.1 map), then fix the rest by hand, one category at a time.

## Error categories (errs.log, 1,323 errors; [`classify-errors.py`](classify-errors.py))

| Category | Count | Automatable? |
|---|---:|---|
| `ResourceLocation`→`Identifier` (type, import, static) | 441 | **Yes**: sed or ChangeType (21.11 post; 1.21.11 primer L23-25) |
| `ResourceKey#location()`→`identifier()` | 62 | **Yes**: derived table |
| `GuiGraphics`→`GuiGraphicsExtractor` (type) | 210 | **Yes**: sed or ChangeType (26.1 post "GuiGraphics rename") |
| GUI `render*`→`extract*` overrides and "not abstract… extract*" | 68 | **Yes**: ChangeMethodName with matchOverrides (26.1 primer L2298) |
| Vanilla 1:1 moves (FastColor→ARGB, critereon, Util, serverLevel, getAllKeys) | 68 | **Yes**: derived table + primer |
| `Ingredient#getItems`→`items` (type change) | 13 | Rename is automatic; call sites need hand fixes |
| PDL package moves (`api.utils`→`api.misc`: RGBAColor, IOAction, PDLDeferredRegisterItems) | 20 | **Yes**: package map (PDL compare 1.1.8…1.1.15) |
| PDL removed APIs (GuiUtils missing from 1.1.15 jar though present in source, HandlerUtils, Sided*/ItemHandler, getItemHandler) | 52 | Hand (transfer rework, [21.9 post](https://neoforged.net/news/21.9release/)) |
| Input-event signatures (`MouseButtonEvent`/`KeyEvent`/`CharacterEvent`) | 114 | Semi: one repeated template (1.21.9 primer L1870-1874) |
| `sendToServer`→`ClientPacketDistributor`, `FMLEnvironment.dist`→`getDist()` | 19 | **Yes**: sed (21.9 post "FML changes"); the ClientPacketDistributor class exists in the NF 26.1.2.109 sources jar |
| NeoForge datagen, capabilities, model APIs, other NF overrides | 38 | Hand ([21.4 post](https://neoforged.net/news/21.4release/) datagen split) |
| Rendering pipeline (RenderType, RenderStateShard, BakedModel, RenderSystem) | 53 | Hand |
| Serialization (SavedDataType, ValueInput/Output, codecs) | 19 | Hand (1.21.5 primer L1276; 1.21.6 primer L1459-1461) |
| Recipes (getResultItem, getIngredients, getRecipeManager) | 19 | Hand |
| ClickEvent/HoverEvent records, isClientSide/private fields | 27 | Mostly hand; `isClientSide`→`isClientSide()` works with sed (1.21.9 primer L1974) |
| Compat classes excluded in `build.gradle` L53-61 but still referenced | 26 | Hand (guard or exclude) |
| Other vanilla semantic changes (getServer, getDayTime, hasPermission, snapback*, …) | ~74 | Hand |

**Automatable share:** about **900 of 1,323 (≈68%)** can be cleared by rename passes, and about 130 more (≈10%) follow a repeated template. **Caveat:** about 200 `GuiGraphics` call sites are hidden at the moment because their receiver type doesn't resolve (grep counts: `blitSprite` 40, `pose` 26, `fill` 22, `drawString` 19, `blit` 12, and more). They will surface after the type rename, some as simple renames (`drawString→text`, `renderItem→item`, per `GuiGraphicsExtractor.java` L236/L832) and some as signature changes (`blitSprite(RenderPipeline, …)` L376). Expect the error count to drop to roughly 500–600, not 400. That figure is an estimate and unverified.

## Suggested workflow

1. Branch off `26.1` (for example `26.1-renames`). Keep `main` (1.21.1) buildable as the reference workspace ([26.1 post](https://neoforged.net/news/26.1release/)).
2. **Text pass (sed, word-boundary):**
   - `ResourceLocation`→`Identifier`
   - `GuiGraphics`→`GuiGraphicsExtractor`
   - `advancements.critereon`→`advancements.criterion`
   - `net.minecraft.Util`→`net.minecraft.util.Util`
   - `FastColor.ARGB32`→`ARGB`
   - `portingdeadlibs.api.utils`→`portingdeadlibs.api.misc`
   - `FMLEnvironment.dist`→`FMLEnvironment.getDist()`
   - `PacketDistributor.sendToServer`→`ClientPacketDistributor.sendToServer`
3. **Member pass:** generate `sed` rules, or an OpenRewrite `ChangeMethodName` YAML, from `rename-1.21.1-to-1.21.11.json`, filtered to the methods the mod actually calls. Also add the 26.1 GUI renames by hand from the 26.1 primer: `render`→`extractRenderState`, `renderWidget`→`extractWidgetRenderState`, `renderBg`→`extractBackground`, `drawString`→`text`, `drawCenteredString`→`centeredText`, `renderItem`→`item`, `hLine`→`horizontalLine`. Mercury or OpenRewrite need the **1.21.1** classpath for type attribution; that's my inference from the OpenRewrite LST docs, unverified for MDG. The simpler route is a grep-driven sed restricted to receivers typed as those classes.
4. Recompile (`scratchpad/tc.sh`) and re-run `classify-errors.py`.
5. Hand-fix by category, largest first:
   - input events (one template)
   - PDL/transfer API
   - rendering
   - datagen
   - serialization
   - recipes
   - then the rest
6. **Unverified:** whether Mercury's cross-version remap works on MDG projects. I didn't test it; a sed pass is enough for the counts above.
