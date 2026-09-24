# 07 — Integrate: compile, datagen, the game loads

**What to build:** The `26.1` branch builds cleanly and the game runs. Datagen output is regenerated. A dedicated server and a client both start with Researchd. In a dev world with the draw at 0, a player can build a Research Lab, stock it with Research Packs, and watch the team's current research progress and complete. This is where the wide refactor turns green.

**Blocked by:** 03, 04, 05, 06

**Status:** agent part done (commit on `26.1`; push left to maintainer). Manual client checks are left to the maintainer.

- [x] Full compile with no errors. `compileJava` is clean. The 1.21.1 `LabEnergyDrawTests` is excluded from the gametest source set until 08 (`TODO(26.1 port, 08)` in `build.gradle`).
- [x] Datagen regenerated, including the 26.1 item model definitions. The output is **not committed**: `src/generated` is in upstream's `.gitignore`, and CI runs `runData` before `build`.
- [x] The dedicated server starts with no Researchd errors in the log
- [ ] The client starts, a world with the default Researchd datapack loads, and the research screen opens. The client, the research screen and a world with the dev test datapack were checked on 2026-09-24. Not yet checked: a world with the default `example_researches` pack enabled.
- [x] Manual: the Research Lab forms, accepts Research Packs through a Lab Part, and completes a research (2026-09-24, dev test datapack, draw at 0)
- [ ] Manual: team create, join and leave work with native teams
- [x] Anything noted by 03–06 (see the Notes in 03) is either fixed or filed as a follow-up ticket

**Notes (for 08 and later tickets):**
- **Agents run Minecraft headless only:** `runServer --nogui`, `runData` and the GameTest server. The maintainer runs the client.
- **Headless server smoke test:**
  - Run `./gradlew runServer --args="net.neoforged.fml.startup.Server --nogui --world <name>"`. `--args` replaces DevLaunch's whole argument list, so the main class goes first.
  - A new server world leaves the default datapack disabled, because `server.properties` has `initial-enabled-packs=vanilla`. 1.21.1 behaves the same (`PackSource.FEATURE`).
  - With the pack enabled, the server loads 9 researches and 3 Research Packs.
- **Ready-made client world:** `run/saves/port26-smoke` has the default datapack enabled.
- **Fixed at runtime (none of these showed at compile time):**
  - `ClientLevelMixin` now matches the 26.1 constructor.
  - `EditBoxMixin` targets `extractWidgetRenderState`.
  - Blocks and items register through `registerBlock`/`registerItem`, so their properties carry the id 26.1 requires.
  - The `research_lab` item keeps the Lab Controller's name (`overrideDescription`).
  - **The default datapack is built in `AddPackFindersEvent`, before item components are bound.** It must not create `ItemStack`s there:
    - `ItemResearchIcon.single(ItemLike)` builds a template.
    - Pack recipes use `ResearchPackImpl.asTemplate`; `asStack` is now `asTemplate(key).create()`.
  - **Payload codecs:** `Research`, `ResearchPack` and `ResearchIcon` use `fromCodecWithRegistriesTrusted`. On 26.1, ingredients (holder sets) need `RegistryOps`; with plain NBT the client disconnected on `update_researches`.
  - **`pack.mcmeta`:** the root file declares formats 84–101 (resources–data). Dark mode declares 84.
  - The default datapack logs research, pack and recipe entries it fails to encode, where it used to drop them silently.
- **Workarounds for PDL bugs (ticket 20, marker `TODO(26.1 port, 20)`):**
  - `DynamicPack` has no pack metadata, so `ResearchdExamplesSource` builds `Pack.Metadata` itself.
  - `registerSimpleItemNoCreative` doesn't set the item id.
  - The `handler_exposure(s)` key mismatch from 04.
- **Visual differences from 05/06:** moved to ticket 19 (needs a person). The three layering markers now point to 19.
- **KubeJS:** `ResearchBuilder.iconPack` now uses `asTemplate`. The excluded KubeJS code still has other 26.1 breakage, such as `iconStacks`; it returns with KubeJS 8.
- **Access transformer:** `accesstransformer.cfg` still lists 1.21.1 targets. Some are gone, such as `GameProfileCache` and `renderFloatingItem`; others are already public on 26.1. NeoForge didn't complain at runtime. Clean it up during release readiness (17).
- **Manual testing aids:**
  - **Creative tab:** the Researchd tab is on page 2 of the creative inventory (26.1 has 14 vanilla tabs). It holds the Research Lab and the 3 Research Packs.
  - **JEI:** it runs in the dev client (`localRuntime`), without Researchd's compat (14).
  - **Test datapack:** `.scratch/port-26.1/dev-datapacks/researchd-lab-test` adds a research pack, `researchd:lab_test_pack` (`data/researchd/researchd/research_pack/`), and a root research, `researchd:lab_test`, that consumes 3 of those packs at 20 ticks each. It's copied into `run/saves/*/datapacks` and `run/port26-smoke/datapacks`. The default packs (overworld/nether/end) come from the `researchd:example_researches` feature pack, which new worlds leave disabled. The research method id is `researchd:consume_research_pack`.
- **Found in the manual client test (2026-09-24):**
  - **Lab screen size:** PDL's container screen blitted the 256x256 background with the screen size as the texture size (404d274). It's the fourth PDL bug in ticket 20.
  - **Start button did nothing:** 26.1's `ContainerEventHandler#mouseClicked` gives a click only to the first child under the mouse, with no fall-through. `ClickDispatch` restores the 1.21.1 dispatch in the research and team screens (978489e).
  - **Edited research never progressed:** a team's saved progress kept the research method from its first save. Fixed by porting upstream's `ResearchProgress.rebindTo` (a0e16a6). Skip that commit in the upstream rebase.
  - **Disconnect when a research completes:** sync payloads held the live team, and the server changed the queue while the network thread was encoding it (`ConcurrentModificationException`). `PayloadSnapshots` copies the state when the payload is built (bb10822). The same race exists on 1.21.1, so it's a candidate for an upstream PR.
  - **Dev config:** `run/config/researchd-common.toml` had `research_lab_energy_usage = 20` left from energy testing. It's set to 0 now, since there's no FE source in the dev client.
  - **1.21.1 team data isn't read:** see ticket 21.
