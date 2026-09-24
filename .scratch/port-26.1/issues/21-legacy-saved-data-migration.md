# 21 — Carry 1.21.1 team data into 26.1 worlds

**What to build:** A 1.21.1 world opened on 26.1 keeps its research teams, queues, progress and team research effects. On 1.21.1, `TeamSavedData` and `TeamResearchEffectSavedData` saved to `data/team_research.dat` and `data/team_research_effect_data.dat`. On 26.1, `SavedDataType` uses the id `researchd:team_research`, and overworld data lives under `dimensions/minecraft/overworld/data/researchd/team_research.dat`. Nothing reads the old files, so an upgraded world starts with no teams. Labs placed before the upgrade then point at team ids that no longer exist, and their tick returns early.

**Blocked by:** 08 — Lab Energy Draw on 26.1 (GameTest wiring)

**Status:** ready-for-agent

- [ ] When the new file is missing and the old one exists, the old file is read (same codec, under `data` → `map`) and saved to the new location
- [ ] The same for `team_research_effect_data`
- [ ] Check whether vanilla or NeoForge already moves unknown `data/*.dat` files during the world upgrade before writing custom code
- [ ] GameTest or headless check: a copy of a 1.21.1 world with a team and a queued research loads with the team, queue and progress intact
- [ ] The old files are left in place (no deletion), so a rollback to 1.21.1 still works

Found during 07's manual test: the `New World` dev save (created on 1.21.1 during energy testing) had `data/team_research.dat` from 2026-09-23, and 26.1 created a fresh team next to it.
