# 09 — Recipe locking at the crafting table

**What to build:** Recipes are Locked again for teams that haven't unlocked them. The shared "is this recipe Locked for this Team Context" check is rewritten for 26.1 recipes with the same rule: a recipe is Locked if its id is Locked, or if its result or any ingredient is a Locked item. The recipe-lookup and crafting-grid hooks are restored, so the crafting player's team is the Team Context.

**Blocked by:** 08 — Lab Energy Draw on 26.1 (GameTest wiring)

**Status:** ready-for-agent

- [ ] GameTest: a mock player in a team without the research gets an empty crafting-table output; after the research completes, the output appears
- [ ] GameTest: a recipe whose ingredient is a Locked item is Locked
- [ ] GameTest: a recipe unlocks without the player relogging
- [ ] Recipe lookups that happen with no Team Context are unaffected
- [ ] The marker from 04 is removed
