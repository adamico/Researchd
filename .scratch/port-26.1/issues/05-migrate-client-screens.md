# 05 — Migrate batch: the client screens

**What to build:** The player-facing client compiles against 26.1's render-state GUI: the screen widget library, the research screen, the Research Lab screen with its energy bar, the team screens, the research info widgets, icons, renderers, the client cache and the client API.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** done (commit on `26.1`; push left to maintainer)

- [x] No compile errors remain in those client packages
- [x] Screens and widgets use the 26.1 render-state extraction methods and the graphics extractor type
- [x] The Research Lab screen still adds its energy bar only when the Lab Energy Draw is above 0
- [x] The recipe-unlock info widget shows only the recipe id and the result icon for now, with a marker pointing to 12
- [x] The red tooltip on Locked items is kept (rename only)
- [x] Layout and visuals are meant to match 1.21.1; differences are noted for the checklist in 07
- [x] No references remain to compat classes that 02 took out of the source set: `ResearchdClient` (IE multiblock effect widget), `compat/RecipeViewerHelper` (`EMICompat`, `JEICompat`, marker to 14)

**Notes (for 06, 07 and later tickets):**
- **No automated tests ran.** Client GUI has no GameTest seam, and the branch still doesn't compile as a whole: the only javac errors left are in the editor packages (105, ticket 06).
- **Shared widget library (06 builds on it):**
  - `AbstractContainerWidget` is a scroll area on 26.1. `AbstractLayoutWidget` and `PopupWidget` pass default scrollbar settings, report `contentHeight() = height` (never scrollable) and hand `mouseScrolled` to the child under the mouse, as on 1.21.1.
  - `LayoutWidget`'s `*Elements` helpers take the 26.1 input events.
  - `PDLButton` renders in `extractContents`; `onPress` takes `InputWithModifiers`.
  - PDL 1.1.15 dropped `GuiUtils`; `drawImg` now lives in `researchd.utils.GuiUtils` (same behaviour).
  - `RdZIndex` keeps only the three constants the editor still uses (marker to 06).
- **Layering:** z-offsets are gone. Popups on the research screen, and the graph drop-down, each get `nextStratum()`; everything else relies on draw order.
- **Visual differences for the 07 checklist:**
  - Ghost Research Packs in the Lab's pack row are drawn normally and covered by a slot-grey overlay (alpha 195), instead of being drawn at 60/255 alpha (26.1 GUI items take no tint).
  - The registry-suggestion drop-down (`SuggestionRegistryVerifyEditBox`) no longer draws above widgets drawn after it.
  - The recipe-unlock widget shows the effect's `icon` (if set), the recipe sprite and the ids in its tooltip; clicking does nothing until 14. No result icon without a custom icon: 26.1 clients have no recipes (12).
  - The Lab model in the BER is submitted with the model's own render layers (`submitMultiLayer`); 1.21.1 forced the translucent entity render type. `ItemBlockRenderTypes.setRenderLayer` is gone in 26.1 (and the controller is `INVISIBLE` anyway).
  - Research Pack tint: `RegisterColorHandlersEvent.Item` is gone; `ResearchPackTintSource` (`researchd:research_pack`) tints layer 1 and `ModelsProvider` now writes a tinted model (this was 03's TODO for 07; 07 still regenerates).
  - Keybinds sit in a registered `KeyMapping.Category` `researchd:researchd`; `EnUsLangProvider` adds its name ("Researchd").
- **Behaviour:**
  - A Research Pack slot scrolled out of the pack row can't be hovered or clicked (1.21.1 only skipped its highlight).
  - `TechListWidget`'s `clicked(...)` hook no longer exists on 26.1; it overrides `mouseClicked` with the 1.21.1 logic.
  - Unused `ResearchdRenderTypes` and the unused standalone `block/research_lab` model registration were removed.
  - Ingredient items for display resolve through `Ingredient#display()` (`CycledItemRenderer`).
- **Text colours:** 26.1 skips text whose colour has alpha 0 (1.21.1 made it opaque); every RGB-only colour in scope now has `0xFF` alpha. 06 should check the editor for the same.
- **Locked tooltip:** `events/client/ResearchdClientEvents` already compiled; its "blocked" wording/identifiers are left alone (spec: renaming is optional cleanup).
