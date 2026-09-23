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

**Lab Energy Draw**:
The optional amount of energy a Research Lab must pay per tick of research. When the Lab cannot pay a tick in full, nothing happens that tick. A draw of zero turns the feature off.
_Avoid_: Power cost, FE usage
