# 06 — Migrate batch: the editor

**What to build:** The in-game research editor compiles against 26.1: the editor screens, the editor objects for methods and effects, and the edit-mode API.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** ready-for-agent

- [ ] No compile errors remain in the editor screen, editor impl or edit-mode packages
- [ ] The editor uses the 26.1 render-state GUI
- [ ] The recipe-unlock editor object no longer reads the client's recipe manager; its picker temporarily accepts a recipe id only, with a marker pointing to 13
- [ ] Item selector widgets still work, using the 26.1 item and template types
