# 06 — Migrate batch: the editor

**What to build:** The in-game research editor compiles against 26.1: the editor screens, the editor objects for methods and effects, and the edit-mode API.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** done (commit on `26.1`; push left to maintainer)

- [x] No compile errors remain in the editor screen, editor impl or edit-mode packages
- [x] The editor uses the 26.1 render-state GUI
- [x] The recipe-unlock editor object no longer reads the client's recipe manager; its picker temporarily accepts a recipe id only, with a marker pointing to 13
- [x] Item selector widgets still work, using the 26.1 item and template types
- [x] No references remain to compat classes that 02 took out of the source set: `DefaultItemSelectorCategory` (`JEICompat`, marker to 14)

**Notes (for 07 and later tickets):**
- **No automated tests ran.** The editor has no GameTest seam. `compileJava` is clean; `compileGametestJava` still fails on the 1.21.1 GameTests (ticket 08).
- **Item selector:** 26.1 ingredients can't hold stacks with components (a Research Pack is one item told apart by its component), so `ItemSelectorWidget` now holds a `List<ItemStack>`. `getSelectedStacks()` feeds packs, item unlocks and icons (`ItemStackTemplate`). `getSelected()` gives an `Optional<Ingredient>` for item methods; an ingredient loaded from an existing method (e.g. a tag) is saved back unchanged. A tag picked in the popup is expanded to its items, as on 1.21.1. Item methods now fail `valid()` with no item selected, where they used to save an empty ingredient.
- **JEI category** removed from `DefaultItemSelectorCategory`; the default is "All Items" (marker to 14).
- **Recipe-unlock picker:** `EditableIdListWidget` takes `null` ids, meaning any well-formed id is valid (`RegistryVerifyEditBox` with neither registry nor ids). Marker to 13.
- **Fixed in passing:** `RegistryVerifyEditBox#getObjectById` returned an `Optional` cast to the value type on 26.1 (`Registry#get`), which crashes the value-modifier effect editor; it now uses `getValue`. Its default text colour and the tag editor's red now have `0xFF` alpha.
- **`RdZIndex` deleted:** z-offsets are gone from the editor.
- **Visual differences for the 07 checklist:**
  - The editor side bar was pushed behind the screen (z −1000); it now covers what the screen drew before it.
  - The pack drop-down in the pack search bar no longer draws above widgets drawn after it.
  - Hover overlays on item selectors and research list entries rely on draw order (drawn after the item/icon).
  - `drawScrollingString` has no colour argument on 26.1; every editor call passed white, which is its default.
