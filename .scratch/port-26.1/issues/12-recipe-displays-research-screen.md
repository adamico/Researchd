# 12 — Recipe displays on the research screen

**What to build:** The research screen draws each recipe a research unlocks, as on 1.21.1, even though 26.1 clients no longer receive full recipes. With the research definitions, the server sends the recipe display of every recipe referenced by a recipe-unlock Research Effect, keyed by recipe key. The recipe-unlock info widget draws from those displays and doesn't need JEI.

**Blocked by:** 08 — Lab Energy Draw on 26.1 (runs right after 07, so the Lab is under test before anything else lands)

**Status:** ready-for-agent

- [ ] Recipe displays for the referenced recipes reach the client on login and after every datapack reload
- [ ] The recipe-unlock info widget draws the recipe grid or layout from the display
- [ ] A recipe that no longer exists after a reload falls back to the id-only view without crashing
- [ ] Manual: the research screen shows the unlocked recipes in a dev world, with and without JEI
- [ ] The marker from 05 is removed
