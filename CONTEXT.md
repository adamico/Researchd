# Researchd

Factorio-style research for Minecraft: teams research technologies by feeding Research Packs into Research Labs.

## Language

### Research Lab

**Research Lab**:
A multiblock made of one Lab Controller and four Lab Parts. It advances its team's current research by consuming Research Packs.
_Avoid_: Lab block, research table

**Lab Controller**:
The central block of a Research Lab. It holds the Lab's packs, energy buffer and progress.
_Avoid_: Master block

**Lab Part**:
One of the four side blocks of a Research Lab. It accepts items and energy on the Lab Controller's behalf.
_Avoid_: Side block, slave block

**Research Pack**:
A consumable item that a Research Lab spends to advance research.
_Avoid_: Science pack

### Research and locks

**Research Effect**:
What completing a research changes for its team, such as unlocking a recipe, an item or a dimension, or adjusting a value.
_Avoid_: Reward, unlock (as a noun)

**Locked**:
The state of a recipe, item or dimension that a team has not yet unlocked through a Research Effect. Locked recipes don't craft or process for that team.
_Avoid_: Blocked, restricted

**Team Context**:
The team whose locks apply to a given recipe lookup: the crafting player's team, or the team of whoever placed the machine doing the lookup.
_Avoid_: Owner, filter frame

### Energy

**Lab Energy Draw**:
The optional amount of energy a Research Lab must pay per tick of research. When the Lab cannot pay a tick in full, nothing happens that tick. A draw of zero turns the feature off.
_Avoid_: Power cost, FE usage
