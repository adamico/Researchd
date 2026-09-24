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

**Research Progression**:
The server-wide lifecycle of a team's research: advancing the current research each tick, completing it, and revoking it. The only way a research becomes Researched or stops being Researched.
_Avoid_: Research manager (that's the catalog of research definitions)

**Complete**:
To make a research Researched for a team, naturally when its progress fills or forced by an admin. It leaves the queue and its Research Effect is applied, whether or not any member is online.
_Avoid_: Unlock (a research), finish

**Revoke**:
The inverse of complete. The research goes back to unresearched with its progress reset, its Research Effect is reversed, and queued researches whose parents are no longer met leave the queue. Completed descendants keep their state.
_Avoid_: Remove (a research), uncomplete

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
