# 03 — Migrate batch: the core model

**What to build:** The mod's core model compiles against 26.1: research, Research Pack and Research Effect definitions, their codecs, the registries, datapack resources, utilities, translations and datagen providers. This is one migrate batch of the wide refactor. The branch doesn't compile as a whole until 07.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** done (commits on `26.1`; push left to maintainer)

- [x] No compile errors remain in the non-client API, data, registries, resources, utils, translations or datagen packages
- [x] `ResourceLocation` → `Identifier`, and the other 26.1 renames, applied
- [x] ItemStack/FluidStack uses in data files and codecs moved to the 26.1 template types where data components are loaded late
- [x] Recipe ids referenced by Research Effects and effect data become recipe resource keys
- [x] Saved data uses the 26.1 storage API
- [x] Behaviour is unchanged apart from API renames; anything that can't migrate mechanically is noted in the ticket for 07
- [x] No references remain to compat classes that 02 took out of the source set: `ResearchEffectSerializers` and `ResearchdEffectDataTypes` (IE multiblock effect), `ReloadableRegistryManager` (`KubeJSCompat`)

**Notes (for 07 and later tickets):**
- **How it was done:**
  - The renames ran as scripted passes over the whole tree (`.scratch/port-26.1/pass1-text-renames.py`, `pass2-member-renames.py`). See `research-upgrade-path.md`.
  - Tree-wide errors went from 1323 to 561 after the rename commit, and to 473 after the ticket-03 hand fixes.
  - Typecheck without Gradle: javac with Gradle's compile classpath. Gradle hangs when it has to forward ~1000 errors.
- **Recipe locking:**
  - `RecipeFilterContext.isBlocked` still checks Locked recipe ids.
  - The "result or ingredient is a Locked item" half is stubbed to false (marker `TODO(26.1 port, 09/10)`). 04 can leave it as is; its mixins are off anyway.
  - `ItemUnlockEffect.getRecipes` was removed: it had no callers, and its matching is the 09 rewrite.
- **Recipe resolution is server-only now.**
  - `RecipeUnlockEffect.getRecipes` and `RecipeUnlockEffectData.resolve` return nothing on a client level.
  - So `RecipeUnlockEffectWidget` shows no recipes until 12 sends recipe displays.
- **Research Pack tint:**
  - The 1.21.1 layer-1 tint (`registerColorHandlers` in `ResearchdClient`) needs a 26.1 item tint source.
  - Datagen writes a plain item model for `research_pack` for now (marker `TODO(26.1 port, 07)` in `ModelsProvider`).
  - No ticket owns this yet. 07 should fix it or file one.
- **Removed with their integrations:**
  - IE: `unlock_ie_multiblock` serializer, `ie_multiblock_unlock` effect type and effect data type.
  - KubeJS: the research and Research Pack merge in `ReloadableRegistryManager`.
- **Datapack format:**
  - Unchanged, except that `item_research_icon` no longer accepts empty entries (`{}`) in `items`. It used `ItemStack.OPTIONAL_CODEC` before.
  - Icons, and the `icon` of the item and recipe unlock effects, are now `ItemStackTemplate`.
- **Save formats changed (no migration, ADR 0002):**
  - Saved data is at `data/researchd/team_research.dat` and `team_research_effect_data.dat`.
  - Attachments are wrapped as `placed_by`, `type` and `settings`.
- **Reload listener:** `ReloadableRegistryManager` now logs parse errors (vanilla `scanDirectory`); 1.21.1 skipped them silently. Files whose path starts with `_` are still dropped, but after parsing.
- **`CheckItemPresenceResearchMethod` / `ConsumeItemResearchMethod`:** the `EMPTY` constants were removed (26.1 `Ingredient` can't be empty; they were unused).
- **Datagen:**
  - `DataGatherer` listens to `GatherDataEvent.Client` (client-only subscriber) and registers every provider.
  - `BlockModelProvider` was replaced by `ModelsProvider` (vanilla `ModelProvider` with NeoForge's OBJ loader template).
  - 07 still has to run it and commit the output.
- **Static `pack.mcmeta` files still use `pack_format`** (`src/main/resources/pack.mcmeta`: 15; `assets/researchd/darkmode/pack.mcmeta`: 48). 26.1 wants `min_format`/`max_format` (resource 84, data 101); fix in 07. `PackWriter` already writes the new fields.
- **Small client fixes forced by the template change:** `ClientItemResearchIcon`, `SimpleResearchObject`, `RecipeUnlockEffectWidget` (use `stacks()` / `create()`).
