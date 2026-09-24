# 20 — PDL fixes for bugs the 26.1 port works around

**What to build:** Fixes to Porting-Dead-Libs (`main`, 26.1) for three bugs Researchd works around on the `26.1` branch. They can go in the same PR as 18. The work happens in a Porting-Dead-Libs checkout, not in this repo. Once a PDL release ships the fixes, Researchd drops its workarounds (each is marked `TODO(26.1 port, 20)`).

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] `GhostMultiblockControllerBE` saves `handler_exposures` but loads `handler_exposure`, so after a reload no Lab Part exposes anything (found in 04). Researchd workaround: `ResearchLabControllerBE.loadAdditional` reads the saved key itself.
- [ ] `DynamicPack#getMetadataSection` always returns null, so `Pack.readMetaAndCreate` rejects the pack ("Missing metadata"). It should return its `PackMetadataSection` for the pack type. Researchd workaround: `ResearchdExamplesSource` builds `Pack.Metadata` itself.
- [ ] `PDLDeferredRegisterItems#registerItemNoCreative` / `registerSimpleItemNoCreative` build `Item.Properties` without `setId`, which crashes on 26.1 ("Item id not set"). Researchd workaround: `ResearchdItems.GREEN_RESEARCH_PACK_ICON` sets the id itself.
- [ ] A PR description is drafted (can share one with 18); opening it upstream is left to the maintainer
- [ ] A follow-up is noted for Researchd: remove the three workarounds once a PDL release ships the fixes
