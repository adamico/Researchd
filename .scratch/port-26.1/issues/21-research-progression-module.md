# 21 — Research Progression: one module completes and revokes research

**What to build:** A server-wide **Research Progression** module (see `CONTEXT.md`) that becomes the only way a team's research advances, is **completed** or is **revoked**. Today the steps are spread across `ResearchdServerTickHandler`, `ResearchTeamImpl.setResearchCompleted`/`onCompleteResearch`/`onRemoveResearch`, `TeamResearches`, three `ResearchCommands` sequences and a client-side copy in `ClientResearchCompletedPayload`. That spread causes the bugs listed below. Design settled in an architecture-review grilling session on 2026-09-24 (candidate 1).

**Blocked by:** 08 — Lab Energy Draw on 26.1, proven by GameTests (its GameTest wiring, and its Lab scenarios as a regression net for the Lab changes here)

**Status:** blocked

## Shape

- One instance per server. It is created on `ServerAboutToStartEvent`, dropped on `ServerStoppedEvent`, and reached through `ResearchProgression.get(MinecraftServer)`. It is not a level-unload-reset static like `ResearchManagerImpl.instance`.
- Research Effects always get the server overworld, so they apply with no member online and from the console.
- The research catalog is passed in, not read from the static `ResearchManagerImpl`.
- `tick()` runs from `ResearchdServerTickHandler`. For each team it:
  - runs the non-Lab methods;
  - compares progress with **the previous tick** (not just around its own `checkProgress` call), so progress the Lab adds during its block entity tick is caught;
  - syncs progress at most every 10 ticks, and always on completion;
  - completes the research when its progress fills.
- `complete(team, Collection<ResourceKey<Research>>)`:
  - marks each research Researched with a server-stamped `long` time;
  - removes it from the queue **wherever it sits**;
  - applies its Research Effect and marks the team changed.
  - After all the effects are applied: one `SyncTeamPayload`, one message per online member (a summary when there's more than one research, "by an admin" when forced), and feedback to the command source.
- `revoke(team, Collection<ResourceKey<Research>>)`:
  - returns each research to unresearched and **resets its progress**;
  - runs `onLock` with the overworld;
  - removes queued researches whose parents are no longer met.
  - **No cascade**: completed descendants stay Researched (write this into the interface docs).
  - Syncs and notifies the same way as `complete`.
- Sending goes through one private method of the module (`PacketDistributor` directly). The Team State Publisher (architecture candidate 2) replaces it later. No sink interface for now: only one adapter would use it.

## Checklist

- [ ] `ResearchProgression` exists with the interface above. `ResearchdServerTickHandler` and `ResearchCommands` call it, and nothing else completes or revokes research.
- [ ] Removed from `api/team/ResearchTeam` and `ResearchTeamImpl`: `setResearchCompleted`, both `onCompleteResearch` overloads, `onRemoveResearch`, and the `forced` flag.
- [ ] `ClientResearchCompletedPayload` no longer changes client team state. It shows the message and refreshes the screen. The server's `SyncTeamPayload` after the effects is authoritative. Its timestamp is a `long`, which fixes the `(int) completionTime` overflow after ~25 days of game clock.
- [ ] `ResearchQueueAddPayload` stops writing the client-sent time into `researchedTime`.
- [ ] `ResearchLabControllerBE.tick` runs on the server only. PDL's ticker has no side check, and the client currently runs its own simulation of the Lab. The rest of the Lab (pack consumption) is left for architecture candidate 4.
- [ ] The team is marked changed on every progress, status and queue change the module makes.
- [ ] GameTests at the module's interface, using a test datapack with a few researches and a recording effect, on 08's wiring:
  - [ ] a natural completion with no member online applies the effect
  - [ ] a forced complete of a queued, non-head research removes it from the queue
  - [ ] a bulk complete applies every effect and sends the team state once
  - [ ] revoke resets progress, so queueing the research again doesn't complete it on the next tick
  - [ ] revoking a parent removes its queued child, and the child is never researched
  - [ ] revoke runs `onLock` with no member online
  - [ ] Lab progress reaches the server team's progress and completes through the module
  - [ ] a console command (no player) doesn't throw
- [ ] 08's Lab Energy Draw GameTests still pass
- [ ] The `TODO(26.1 port, KubeJS)` research-completed event marker moves into the module's complete path

## Out of scope

- Reload: rebinding saved progress, `cleanupTeamResearches`, `initializeTeamResearches`, and the `onLock`-per-research effect initialisation in `ResearchTeamHelperServer:591-752`. This is a follow-up that pairs with the Team State Publisher's reload-burst fix.
- Lab pack consumption and the PDL workarounds (architecture candidate 4).
- A cascading revoke.
- Porting to 1.21.1 `main`. Only the three bugs go there, as separate fixes: `.scratch/main-fixes/issues/01`–`03`.

## Bugs this fixes (found by reading the code, not reproduced)

- Research Effects are applied or reversed only when a team member is online (`ResearchTeamImpl:251-270, 283-296`). A Lab that finishes overnight unlocks nothing.
- The `/researchd` complete and revoke commands NPE from the console (`source.getPlayer().getUUID()`), and apply effects only when the command's player is in the target team.
- A forced complete leaves the research in the queue, while the client removes its queue head, so the two queues drift apart.
- A revoked research keeps full progress and completes on the next tick if queued again.
- A queued child of a revoked parent turns Locked but is still researched: the tick doesn't check status.
- `SyncTeamPayload` is sent before `onUnlock`, so clients miss value-effect changes until some later sync.
- Queue edits and progress never mark the team changed, so saving them depends on some unrelated change.
