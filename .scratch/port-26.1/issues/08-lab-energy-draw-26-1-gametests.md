# 08 — Lab Energy Draw on 26.1, proven by GameTests

**What to build:** The GameTest server on 26.1 runs the same Lab Energy Draw scenarios as ticket 01, rewritten for 26.1's data-driven test instances, and they pass. This sets up the 26.1 GameTest wiring (registration, structures, and helpers for a Research Lab and a team) that later tickets reuse.

**Blocked by:** 01 — Lab Energy Draw GameTests on 1.21.1; 07 — Integrate

**Status:** agent part done (commit 5365ce8 on `26.1`; push left to maintainer; the manual GUI check is left to the maintainer)

- [x] The 26.1 GameTest wiring is in place and documented in the ticket's closing note, so 09–11 can reuse it
- [x] All five scenarios from 01 have identical setup and assertions, and pass
- [x] Energy supplied through a Lab Part via the 26.1 energy capability; extracting through a Lab Part gets nothing
- [ ] Manual: the GUI energy bar shows when the draw is above 0 and is hidden at 0; config edits take effect after a reload

**Notes from implementation:**
- **Run:** `./gradlew runGameTestServer` (headless). It runs 6 required tests: the 5 below plus vanilla's `minecraft:always_pass`. On a pass it prints "All 6 required tests passed". A failing test fails the Gradle task.
- **26.1 GameTest wiring (for 09–11), all in `src/gametest/java/.../gametest/`:**
  - `GameTestCase(name, maxTicks, function)`: one test. `registerFunctions(RegisterEvent, cases)` puts the functions in the `test_function` registry. `registerInstances(RegisterGameTestsEvent, environment, cases)` registers a `FunctionGameTestInstance` for each case on the `researchd:empty_7x7x7` structure. Ids are `researchd:<name>`, grouped by a path prefix such as `lab_energy_draw/…`.
  - A test class is an `@EventBusSubscriber(modid = Researchd.MODID)` that calls both from `RegisterEvent` and `RegisterGameTestsEvent`. See `LabEnergyDrawTests`.
  - **Batches are environments.** Tests that share a `TestEnvironmentDefinition` run in one batch, and batches run one after another. Global state (config) goes in a custom environment whose `setup` returns the old value and whose `teardown` restores it. The environment's `MapCodec` has to be registered in `Registries.TEST_ENVIRONMENT_DEFINITION_TYPE`, as `LabEnergyDrawTests.DrawEnvironment` is. Tests with no special state can register an empty `new TestEnvironmentDefinition.AllOf()`.
  - `TestTeams.create(helper, player)` returns a fresh team owned by the player. `TestTeams.queue(helper, team, research)` queues a research.
  - `TestLab.place(helper, player)` places a Research Lab as the player (so it belongs to the player's team) and returns a Lab Part's item and energy handlers (`Capabilities.Item.BLOCK` / `Capabilities.Energy.BLOCK`). It has helpers for insert, item count and energy (each in its own transaction). The Lab takes packs from tick 1.
  - `helper.makeMockPlayer(GameType.SURVIVAL)` is a plain `Player`, not a `ServerPlayer`.
  - **Pass the expected value first to `assertValueEqual`.** 26.1's message ("Expected %s to be %s: was %s") treats the first argument as the expected one.
- **Scenarios:** same setup, ticks, timeouts (100, or 300 for fully powered) and assertions as 01. Two ids were renamed: `…_blocks_research` became `…_stalls_research`, because "blocked" is the glossary's avoided word for Locked.
- **Mutation check:** letting `tryConsumeEnergy` pay without enough energy fails 3 of 5 (empty buffer, less than one tick, fully powered). That's the same as on 1.21.1.
- **Leftover:** the run configs still pass `neoforge.enabledGameTestNamespaces`, a 1.21.1 property. On 26.1 vanilla's `always_pass` runs anyway. It does no harm; clean it up in 17.

