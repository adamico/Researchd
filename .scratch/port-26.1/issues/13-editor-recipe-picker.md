# 13 — Recipe picker in the editor

**What to build:** When an operator opens the research editor, the recipe picker lists every recipe in the game. On opening, the client asks the server once for every recipe key with its display, then caches the reply until the next datapack reload. Non-operators can't request it.

**Blocked by:** 12 — Recipe displays on the research screen

**Status:** ready-for-agent

- [ ] A request payload and a reply payload, registered and restricted to operators
- [ ] The client cache is cleared on datapack reload and filled again the next time the editor opens
- [ ] The picker searches and selects recipes as on 1.21.1, drawing entries from the displays
- [ ] Manual: create a research with a recipe-unlock Research Effect in the editor, save it, and it applies
- [ ] The marker from 06 is removed
