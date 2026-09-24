# 04 — Migrate batch: server logic and the Research Lab

**What to build:** Server-side logic compiles against 26.1: teams, research progress, research methods and effects, networking payloads, events, block entities and the vanilla mixins. The Research Lab moves onto Porting-Dead-Libs handlers: items through a PDL resource handler, energy through a PDL simple energy handler. The Lab Energy Draw pays each tick through a transfer-API transaction that commits only when the full draw was extracted. Lab Parts expose the Lab Controller's items, plus an insert-only energy wrapper, on every side (strict parity, even at draw 0). Recipe locking is temporarily switched off so this batch stays mechanical.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** done (commit on `26.1`; push left to maintainer)

- [x] No compile errors remain in the impl, events, networking, content or vanilla-mixin packages
- [x] Block entities save and load through 26.1 value I/O
- [x] The Lab Energy Draw rules are unchanged: all or nothing per tick, the check sits after the check that Research Packs are present and before a pack is used, a draw of 0 turns it off, capacity is also the transfer limit, and the client sync stays throttled
- [x] Lab Parts: item handler and insert-only energy on every side; the Lab Controller exposes nothing yet (16 adds null-side access)
- [x] The config write-back workaround is still in place and wired to the 26.1 config events
- [x] The Research Effects' server-side recipe resolution uses the 26.1 recipe map
- [x] Recipe locking temporarily switched off: the recipe-lookup, crafting-grid and block-entity-ticking mixins are removed from the mixin config, and the shared "is this recipe Locked" check answers "never Locked", with a marker pointing to 09 and 10
- [x] The Locked-dimension mixin is removed from the mixin config, with a marker pointing to 11
- [x] The reload-listener hook targets the 26.1 server-resources constructor
- [x] Effect data is still synced to all players (parity)
- [x] No references remain to compat classes that 02 took out of the source set: `ResearchTeamImpl` (`KubeJSCompat`), `ResearchdLifecycleHandler` (`FTBTeamsCompat`, marker to 15), `ExampleCommands` (`KubeJSCompat`, `KubeJSExample`)

**Notes (for 05, 07 and later tickets):**
- **No automated tests ran.** The branch doesn't compile as a whole until 07, and the GameTests arrive in 08. Typecheck is javac with Gradle's compile classpath (see 03). After this ticket, the only errors left outside `client/` and `ResearchdClient` are in `compat/RecipeViewerHelper`, which 05 owns.
- **Lab Controller API (for 05):**
  - `getItemHandler()` returns a PDL item stacks handler; `getEnergyHandler()` returns a PDL simple energy handler.
  - `ResearchLabBER` still calls the removed `getItemHandlerStacksList`. Use `getItemHandler().copyToList()` instead.
- **Block entity sync:** PDL 1.1.15's container base class no longer sends block entity data to the client. The Lab Controller now provides its own update packet and tag (full save), plus a private `updateData()`.
- **PDL handler-exposure bug:**
  - `GhostMultiblockControllerBE` saves `handler_exposures` but loads `handler_exposure`. After a reload no Lab Part would expose anything.
  - `ResearchLabControllerBE.loadAdditional` reads the saved key back itself. Remove this workaround once PDL fixes the bug; worth adding to the PDL PR (see spec, Config).
  - Lab Parts still go through PDL's `tryAndGetCapability`, so only the four exposing parts (`I` in the shape) answer, as on 1.21.1.
- **Energy:**
  - The Lab Part energy wrapper is NeoForge's `LimitingEnergyHandler(controller, MAX, 0)`, so it is insert-only.
  - A tick is paid inside `Transaction.openRoot()` and committed only on a full extract.
- **Pack consumption** now goes through the handler's `set`, which marks the Lab changed and sends a block update. On 1.21.1, `shrink(1)` changed the stack in place without notifying anything.
- **Reload listeners:**
  - NeoForge 26.1 builds the listener list from `AddServerReloadListenersEvent`. It throws if a mod adds a listener to `ReloadableServerResources#listeners()` by mixin.
  - The mixin now only creates the managers in the 26.1 constructor. `events/common/ResearchdLifecycleHandler` registers them as `researchd:researches` and `researchd:research_packs`.
- **Recipe locking is off:**
  - `RecipeFilterContext.isBlocked` returns false.
  - Its marker keeps the recipe-id rule for 09 to restore.
- **Mixins out of the config:**
  - `RecipeManagerMixin` and `CraftingMenuMixin` (marker to 09).
  - `BoundTickingBlockEntityMixin` (marker to 10).
  - `PlayerMixin` (marker to 11). It already overrides `Entity#canTeleport`, 26.1's name for `canChangeDimensions`.
  - All four still compile.
- **KubeJS:**
  - The research-completed event and the `/researchd example kubejs` command are removed, with `TODO(26.1 port, KubeJS)` markers.
  - No ticket owns KubeJS; it returns when KubeJS 8 leaves beta.
- **FTB Teams:** the server-start `FTBTeamsCompat.init()` hook is replaced by a marker to 15.
- **Other 26.1 mappings used:**
  - day time → `getOverworldClockTime()`.
  - `ServerPlayer#getServer()` → `level().getServer()`.
  - the profile cache → `services().nameToIdCache()`.
  - `hasPermission(2)` → `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`.
  - `ENTITYBLOCK_ANIMATED` → `INVISIBLE`.
  - The Lab Controller's rotation type is `NONE`.
- **`/researchd dev recipes-dump`** now matches results through recipe displays and ingredients through placement info.
