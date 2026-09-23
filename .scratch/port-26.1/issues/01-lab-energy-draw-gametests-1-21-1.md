# 01 — Lab Energy Draw GameTests on 1.21.1

**What to build:** On `lab-energy` (1.21.1), the GameTest server runs the Lab Energy Draw scenarios against a real Research Lab and passes. These tests are the reference behaviour the 26.1 port must match. They are not pushed to the upstream PR: `lab-energy` is the PR's head branch, so these commits stay local and reach the fork only through `26.1`. Each test builds a Research Lab from a structure, gives a team a current research, stocks Research Packs and supplies energy through a Lab Part, ticks the world, then checks only what a player could see.

**Blocked by:** None — can start immediately

**Status:** done (commits 101f616, 2fdf315 on lab-energy; not pushed to the PR by decision)

- [x] Draw 0: the Lab progresses and ignores energy
- [x] Draw above 0 with an empty buffer: no progress, no Research Pack used, no energy taken
- [x] Less than one tick's draw stored: no progress, no Research Pack used, energy unchanged
- [x] Fully powered: exactly one tick's draw is taken per tick of progress, and Research Packs are used at the normal rate
- [x] Energy inserted through a Lab Part is accepted; extracting through a Lab Part gets nothing
- [x] The gameTestServer run passes on a clean checkout
- [x] Tests set the Lab Energy Draw config per test and restore it afterwards
- [x] Not pushed to the upstream PR (decision: the GameTests stay off the PR)

**Notes from implementation:**
- Tests live in the `gametest` source set: `src/gametest/java/.../gametest/LabEnergyDrawTests.java`, with the template `src/gametest/resources/data/researchd/structure/empty_7x7x7.nbt`.
- The Lab is placed with `ResearchLabItem.place` by a mock player whose team is fresh. The structure is rotated, so the Lab is found by searching around the clicked position.
- The controller sets up its pack slots in `onLoad`, which runs on the first tick, so the Lab is stocked at tick 1.
- The draw is set by `@BeforeBatch`/`@AfterBatch` on the batches `lab_energy_draw_on` and `lab_energy_draw_off`. Batches run one after another.
- Found and fixed along the way (separate commit 101f616): new teams all shared the `TeamResearches.EMPTY` queue and progress. The 26.1 port needs the same fix.
- Mutation check: letting `tryConsumeEnergy` pay without enough energy fails 3 of the 5 tests.

**Open:** `101f616` (new teams shared one research queue, progress and invite list through the mutable `EMPTY` singletons) is a real 1.21.1 bug fix, not test code. It is off the upstream PR along with the GameTests. Decide whether to send it upstream on its own.
