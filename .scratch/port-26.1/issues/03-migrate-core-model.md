# 03 — Migrate batch: the core model

**What to build:** The mod's core model compiles against 26.1: research, Research Pack and Research Effect definitions, their codecs, the registries, datapack resources, utilities, translations and datagen providers. This is one migrate batch of the wide refactor. The branch doesn't compile as a whole until 07.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** ready-for-agent

- [ ] No compile errors remain in the non-client API, data, registries, resources, utils, translations or datagen packages
- [ ] `ResourceLocation` → `Identifier`, and the other 26.1 renames, applied
- [ ] ItemStack/FluidStack uses in data files and codecs moved to the 26.1 template types where data components are loaded late
- [ ] Recipe ids referenced by Research Effects and effect data become recipe resource keys
- [ ] Saved data uses the 26.1 storage API
- [ ] Behaviour is unchanged apart from API renames; anything that can't migrate mechanically is noted in the ticket for 07
- [ ] No references remain to compat classes that 02 took out of the source set: `ResearchEffectSerializers` and `ResearchdEffectDataTypes` (IE multiblock effect), `ReloadableRegistryManager` (`KubeJSCompat`)
