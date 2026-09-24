# 19 — Client visual parity pass

**What to build:** A person runs the 26.1 dev client next to 1.21.1 and works through the visual differences that 05 and 06 recorded. Each one ends up either accepted as a 26.1 difference or fixed. Agents can't do this ticket alone: it needs someone looking at the client (agents run Minecraft headless only).

**Blocked by:** 07 — Integrate

**Status:** needs-human

- [ ] Ghost Research Packs in the Lab's pack row: slot-grey overlay instead of 60/255 alpha (05)
- [ ] Registry-suggestion drop-down (`SuggestionRegistryVerifyEditBox`) no longer draws above later widgets (05; marker `TODO(26.1 port, 19)`)
- [ ] Recipe-unlock widget: effect icon, recipe sprite, ids in tooltip; no result icon until 12 (05)
- [ ] Lab model in the BER drawn with the model's own render layers (05)
- [ ] Research Pack tint on every pack variant (05)
- [ ] Editor side bar now covers what the screen drew before it (06; marker `TODO(26.1 port, 19)` in `EditorSideBarWidget`)
- [ ] Pack drop-down in the pack search bar no longer draws above later widgets (06; marker `TODO(26.1 port, 19)` in `SelectPackSearchBarWidget`)
- [ ] Hover overlays on item selectors and research list entries (06)
- [ ] Any fix removes its `TODO(26.1 port, 19)` marker
