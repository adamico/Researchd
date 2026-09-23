---
title: Port Researchd to Minecraft 26.1.2 (NeoForge), including the Lab Energy Draw
status: ready-for-agent
created: 2026-09-23
---

## Problem Statement

Researchd only exists for Minecraft 1.21.1. Players and pack makers moving to Minecraft 26.1.2 on NeoForge can't use it. Its library (Porting-Dead-Libs) and most of its integrations have moved on to 26.1. Upstream hasn't started a port.

The newest feature, the optional Lab Energy Draw, exists only as an open upstream PR on 1.21.1. It is built on an energy API that NeoForge has deprecated for removal.

There are no automated tests, so nothing guards the behaviour a port is most likely to break: what a Research Lab does each tick, and which recipes are Locked for which team.

## Solution

Create a `26.1` branch on the fork. It carries Researchd, including the Lab Energy Draw, to Minecraft 26.1.2 / NeoForge 26.1.2. Gameplay stays the same as on 1.21.1 across every feature that survives the port.

Integrations that already have a 26.1.2 build come along: JEI, FTB Teams. Those without one are left out of the build for now: EMI, Immersive Engineering, Create, and KubeJS (whose 26.1 version is still in beta).

Jade support is new. Looking at any block of a Research Lab shows its energy, its stocked Research Packs, and the current research with its progress.

GameTests check the Lab Energy Draw rules and recipe locking. Once they pass, unofficial builds are published on the fork's GitHub Releases until upstream decides whether to take the port.

## User Stories

### Players: Research Lab

1. As a player, I want to build a Research Lab on 26.1.2 exactly as on 1.21.1 (one Lab Controller, four Lab Parts), so that my knowledge of the mod carries over.
2. As a player, I want to insert Research Packs through any Lab Part, so that automation works as before.
3. As a player, I want the Research Lab to advance my team's current research by consuming Research Packs, so that research progresses as before.
4. As a player on a server with no Lab Energy Draw, I want the Research Lab to need no power and show no energy bar, so that the feature is invisible when switched off.
5. As a player on a server with a Lab Energy Draw, I want to power the Research Lab by connecting an energy pipe or cable to any Lab Part, so that I can supply it the same way I supply items.
6. As a player, I want a Research Lab that can't pay for a whole tick to do nothing that tick (no progress, no energy taken, no Research Pack used), so that a weak power supply never wastes packs.
7. As a player, I want a fully powered Research Lab to take exactly one tick's draw per tick of progress, so that the cost is predictable.
8. As a player, I want the Research Lab's energy bar in the GUI whenever the Lab Energy Draw is above zero, so that I can see how much power the Lab has.
9. As a player, I want energy pipes not to be able to pull energy out of a Research Lab, so that another machine can't drain it.
10. As a player, I want pipes to keep connecting only to Lab Parts and not to the Lab Controller, so that my builds behave the way they did.
11. As a player, I want breaking a Research Lab to drop its contents as before, so that I don't lose items.

### Players: research and recipe locking

12. As a player, I want recipes to stay Locked for my team until we complete the research that unlocks them, so that research keeps its meaning.
13. As a player, I want the crafting table's output slot to stay empty for a Locked recipe, so that I can't bypass research.
14. As a player, I want a recipe to count as Locked when its result or any ingredient is a Locked item, so that locking an item locks everything built from or into it.
15. As a player, I want a machine placed by a member of my team to refuse to process recipes that are Locked for my team, so that automation can't bypass research.
16. As a player in another team, I want machines placed by my team to use my team's unlocks, so that each team's progress stays separate.
17. As a player, I want a portal to refuse to take me into a Locked dimension (creative mode excepted), so that dimension research keeps working.
18. As a player, I want Locked items to show a red tooltip line, so that I know why I can't use them.
19. As a player, I want a recipe to unlock the moment my team completes the research, without relogging, so that progress feels immediate.
20. As a player, I want the research screen to draw each recipe a research unlocks, the way it does on 1.21.1, even though the 26.1 client no longer receives full recipes.
21. As a player, I want the research screen to show recipes without JEI installed, so that JEI stays optional.
22. As a player with JEI installed, I want to open a recipe's JEI page from Researchd the way I can today, so that the integration still works.
23. As a player with JEI installed, I want each Research Pack variant to appear as a separate JEI entry, as it does today.

### Players: teams

24. As a player on a server with FTB Teams, I want Researchd to use FTB Teams, as it does on 1.21.1.
25. As a player on a server without FTB Teams, I want Researchd's own teams to work: create, join, leave and transfer.

### Players: Jade

26. As a player with Jade installed, I want to look at a Research Lab and see its stored energy, so that I can check power without opening the GUI.
27. As a player with Jade installed, I want to see the Research Packs stocked in the Lab, so that I know when to refill it.
28. As a player with Jade installed, I want to see the research the Lab is working on and its progress.
29. As a player with Jade installed, I want looking at any Lab Part to show the same information as looking at the Lab Controller, so that it doesn't matter which block I aim at.
30. As a player with Jade installed on a server where the Lab Energy Draw is 0, I accept that the energy line still shows the buffer, since the buffer exists and takes power.
31. As a player without Jade, I want Researchd to work normally, so that Jade stays optional.

### Server operators

32. As a server operator, I want to set the Lab Energy Draw and the Lab's energy buffer capacity in the config file and have the values take effect, so that I can make research cost power.
33. As a server operator, I want the Lab Energy Draw to default to zero, so that installing the mod changes nothing until I opt in.
34. As a server operator, I want a clean 26.1 world to be enough to start using Researchd, and I accept that 1.21.1 worlds won't carry over.
35. As a server operator, I want the mod to start without EMI, Immersive Engineering, Create or KubeJS installed.
36. As a server operator, I want effect data synced to clients the same way as on 1.21.1, so that the port doesn't change network behaviour.

### Pack makers

37. As a pack maker, I want to define researches, Research Packs and Research Effects through datapacks on 26.1.2 exactly as on 1.21.1, so that my existing research tree still works.
38. As a pack maker, I want the default Researchd datapack to remain available at world creation.
39. As a pack maker, I want the in-game research editor's recipe picker to list every recipe in the game, even though the client no longer receives full recipes.
40. As a pack maker, I want the recipe picker's list to refresh after a datapack reload.
41. As a pack maker, I understand that KubeJS scripting for Researchd will come back once KubeJS 8 leaves beta, and that datapacks work in the meantime.

### Maintainers

42. As the maintainer, I want the port on its own long-lived `26.1` branch, so that each MC version line evolves independently (ADR 0001).
43. As the maintainer, I want the port to start from the Lab Energy Draw branch, so that the energy feature and the config write-back fix come along automatically.
44. As the maintainer, I want GameTests for the Lab Energy Draw on 1.21.1 first, so that they define the reference behaviour. They stay off the upstream PR.
45. As the maintainer, I want the same scenarios rewritten as 26.1 GameTests, so that the port provably behaves like 1.21.1.
46. As the maintainer, I want GameTests for recipe locking on 26.1, so that the rewritten recipe hooks are guarded.
47. As the maintainer, I want the excluded integrations to stay in the source tree but out of compilation, so that bringing one back is a small build change.
48. As the maintainer, I want the mod version to be 1.3.0 on both lines when they carry the same features.
49. As the maintainer, I want the config write-back workaround kept until Porting-Dead-Libs ships a fix, and a fix offered upstream to Porting-Dead-Libs.
50. As the maintainer, I want to publish unofficial 26.1 builds on the fork's GitHub Releases only after every GameTest passes.
51. As an upstream maintainer, I want the port to be offerable as a `26.1` branch later, with a clean history that contains no local agent files.

## Implementation Decisions

### Branch, versions, build

- The branch is `26.1` on `adamico/Researchd`, started from the tip of `lab-energy`. The mod version is 1.3.0 and the supported MC range is `[26.1.2, 26.2)`.
- The build moves to Java 25, ModDevGradle 2.0.14x and NeoForge 26.1.2.109. Parchment is removed because Minecraft 26.1 ships unobfuscated.
- Dependencies:
  - Porting-Dead-Libs 1.1.15
  - JEI 26.1.2 API (29.40.x)
  - FTB Teams / FTB Library 26.1.2
  - Jade 26.1.11 (Modrinth maven; compile against the full jar, since no API jar is published)
- The dev client runs JEI instead of EMI.
- Excluded from compilation: EMI, Immersive Engineering, Create and KubeJS.
  - Their compat packages stay in the tree, left out of the source set.
  - Their mixins come out of the mixin config. The config is `required`, so a mixin whose target is missing would crash the game.
- Mechanical API migration covers:
  - identifier and registry renames
  - saving block entities through value I/O
  - screens moving to render-state extraction
  - the item and recipe changes in 26.1
  - saved data storage renames
  - regenerating datagen output, including item model definitions

### Research Lab and the Lab Energy Draw

- Behaviour is identical to the 1.21.1 feature:
  - The draw per tick comes from config and defaults to 0. At 0 there is no energy bar and no drain.
  - A Lab pays for a whole tick or does nothing that tick.
  - The payment check sits after the check that Research Packs are present, and before a pack is used up.
  - The buffer capacity comes from config, and it is also the per-tick transfer limit.
- The Lab Controller keeps its items and energy on Porting-Dead-Libs handlers, the same mechanism its ghost-multiblock base class uses:
  - energy is a PDL simple energy handler registered by capability
  - items are a PDL resource handler
  - the removed PDL helpers and NeoForge's deprecated energy and item APIs are no longer used
- Paying for a tick runs as a transfer-API transaction. Energy is extracted inside the transaction, which is committed only if the full draw came out. Otherwise it rolls back.
- Lab Parts expose the Lab Controller's item handler and an insert-only energy wrapper on every side, including when the draw is 0 (strict parity).
- New: the Lab Controller exposes its energy and item handlers **only when queried with no side**, which is how Jade reads blocks. Pipes and hoppers always ask with a side, so they still can't connect to the controller.
- Energy sync to the client keeps its current throttled interval, and still only syncs while the draw is above 0.

### Recipe locking

The parity checklist is every place that locks something on 1.21.1, apart from the Create and Immersive Engineering hooks, which leave with their integrations:

- recipe lookup by input (rewrite)
- recipe lookup by type (rewrite)
- the crafting grid's result update, which sets the crafting player's Team Context (rewrite)
- block-entity ticking under the placer's Team Context (adapt)
- recording the placer's team when a block is placed (rename)
- refusing to change into a Locked dimension (adapt to the renamed entity method)
- the client tooltip on Locked items (rename)

The shared "is this recipe Locked for this Team Context" check is rewritten for 26.1 recipes. The rule stays the same: a recipe is Locked if its id is Locked, or if its result or any ingredient is a Locked item.

- Recipe identifiers used by Research Effects and effect data become recipe resource keys.
- Recipe resolution that used the client's recipe manager moves to the server.
- The reload-listener hook into server resources is re-targeted to the 26.1 constructor.
- **Recipe displays for the research screen.** The server sends clients the recipe display of every recipe referenced by a recipe-unlock Research Effect, together with the research definitions. The research screen's recipe widget draws from these displays.
- **Recipe displays for the editor.** When an operator opens the research editor, the client asks the server once for every recipe id and its display. The server replies. The client caches the result until the next datapack reload.
- **Parity item.** Effect data is still synced to all players, not only the owning team.

### Jade integration (new, 26.1 only)

- A Jade plugin, loaded only when Jade is present.
- Energy and stocked Research Packs come from Jade's built-in energy and item readers, through the no-side capabilities. Nothing Researchd-specific is registered for them.
- The plugin adds a server data provider and a tooltip line for the current research and its progress. It is registered for both the Lab Controller and the Lab Part blocks. On a Lab Part it reads the Lab Controller's data, so both show the same thing.

### Config

- The workaround that copies loaded config values into the annotated config fields stays until a Porting-Dead-Libs release fixes it. The fix is offered to Porting-Dead-Libs as a separate PR.

### Glossary and records

- The spec and code discussions use the `CONTEXT.md` vocabulary: Research Lab, Lab Controller, Lab Part, Research Pack, Research Effect, Locked, Team Context, Lab Energy Draw.
- ADR 0001: one long-lived branch per MC version.
- ADR 0002: no migration of 1.21.1 worlds.

## Testing Decisions

- **One automated seam: NeoForge GameTests against a real server world.** Tests use only what a player or another mod could use:
  - building a Research Lab from a structure
  - giving a team a current research
  - inserting items and energy through Lab Part capabilities
  - ticking the world
  - acting as a mock player at a crafting table or in a portal

  Assertions are on what a player could observe: research progress, Research Packs left, energy stored, the crafting output slot, whether a dimension change is allowed. No test reaches into private fields or relies on how a class is structured internally.
- **Lab Energy Draw scenarios** (written first on 1.21.1 at `lab-energy`, then rewritten for 26.1):
  1. With the draw at 0, the Lab progresses and energy is ignored.
  2. With the draw above 0 and an empty buffer: no progress, no Research Pack used, no energy taken.
  3. With less than one tick's draw stored: no progress, no Research Pack used, energy unchanged.
  4. Fully powered: exactly one tick's draw taken per tick of progress, and Research Packs used at the normal rate.
  5. Lab Parts accept inserted energy, and extraction through a Lab Part gets nothing.
- **Recipe locking scenarios** (26.1):
  1. A mock player in a team without the research gets an empty crafting output. After the research is completed, the output appears.
  2. A recipe whose ingredient is a Locked item is Locked too.
  3. A machine placed by a team member refuses a Locked recipe under the placer's Team Context, and processes it once unlocked.
  4. A Locked dimension refuses a non-creative player.
- **The API gap between versions.** The GameTest API differs between the lines: 1.21.1 uses annotated test holders, while 26.1 uses data-driven test instances. Each branch has its own test wiring, and the scenarios and assertions are kept identical.
- **No prior art.** The repo has no tests. The GameTest server run is configured but empty, so these tests set the pattern.
- **Manual client checklist** (run before each release), for things a GameTest can't see:
  - the GUI energy bar shows when the draw is above 0 and is hidden at 0
  - the Jade lines (energy, Research Packs, current research and progress) show on both the Lab Controller and a Lab Part
  - the research screen draws recipes, with and without JEI
  - the editor's recipe picker lists all recipes and refreshes after a reload
  - the red tooltip shows on Locked items
  - JEI subtypes and recipe pages work
  - FTB Teams and native teams both work

## Out of Scope

- EMI, Immersive Engineering and Create integrations, until each has a 26.1.2 build.
- KubeJS integration, until KubeJS 8 is out of beta.
- Loading 1.21.1 worlds or data on 26.1 (ADR 0002).
- Jade support on the 1.21.1 line.
- Any change to Lab Energy Draw behaviour, including hiding the energy capability or the Jade energy line when the draw is 0.
- Restricting effect-data sync to the owning team. It's a later change on both lines.
- Renaming the code's "blocked" identifiers to "Locked", which is optional cleanup.
- Hiding Locked recipes in the recipe book or in JEI. 1.21.1 doesn't do this either.
- A multi-version build tool such as Stonecutter (ADR 0001).
- CI workflows.
- Publishing to the upstream CurseForge or Modrinth projects.

## Further Notes

- **Milestones:**
  - M0: Lab Energy Draw GameTests on `lab-energy` (1.21.1)
  - M1: build setup and integration exclusions
  - M2: the core compiles and the game loads
  - M3: Research Lab on PDL handlers, with the energy GameTests rewritten for 26.1
    - Ticket 08 is the only work that opens once 07 is done; 12 and 15 wait for it too, so the Lab is under test before anything else lands.
  - M4: recipe-locking parity and its GameTests
  - M5: JEI and FTB Teams
  - M6: Jade
  - M7: every GameTest passes and the manual checklist is done
- **Steps the maintainer takes personally:**
  - opening the Porting-Dead-Libs config PR
  - publishing GitHub Releases
- **Open risks:**
  - Porting-Dead-Libs 1.1.15 targets NeoForge 26.1.0 and hasn't been tested on 26.1.2.
  - JEI's 26.1 build is marked beta.
  - Whether FTB Library needs Architectury at runtime isn't confirmed.
- `CONTEXT.md`, `docs/adr/` and `.scratch/` are local-only and must never reach upstream PRs.
