# 05 — Migrate batch: the client screens

**What to build:** The player-facing client compiles against 26.1's render-state GUI: the screen widget library, the research screen, the Research Lab screen with its energy bar, the team screens, the research info widgets, icons, renderers, the client cache and the client API.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** ready-for-agent

- [ ] No compile errors remain in those client packages
- [ ] Screens and widgets use the 26.1 render-state extraction methods and the graphics extractor type
- [ ] The Research Lab screen still adds its energy bar only when the Lab Energy Draw is above 0
- [ ] The recipe-unlock info widget shows only the recipe id and the result icon for now, with a marker pointing to 12
- [ ] The red tooltip on Locked items is kept (rename only)
- [ ] Layout and visuals are meant to match 1.21.1; differences are noted for the checklist in 07
- [ ] No references remain to compat classes that 02 took out of the source set: `ResearchdClient` (IE multiblock effect widget), `compat/RecipeViewerHelper` (`EMICompat`, `JEICompat`, marker to 14)
