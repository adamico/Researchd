# 26.1 recipe blocking checks actual inputs and falls through Blocked matches

26.1 recipes have no fixed result or ingredient list, so the 1.21.1 rule (Blocked if the result or any item any ingredient accepts is Blocked) had to be rewritten anyway. We took the chance to change it, breaking the port's gameplay-parity promise for this one rule: a recipe is Blocked when its id is Blocked, when the result it makes is a Blocked item (the display's result, plus the assembled output when there is an input), or when an item actually in its input is a Blocked item. Where there is no input (the addon `isBlocked(holder)` API and by-type recipe lists), an ingredient counts only when every item it accepts is Blocked. The lookup filters the stream of matching recipes, so a Blocked first match falls through to the next one that isn't Blocked.

## Considered Options

- **Parity with 1.21.1.** Rejected: one Blocked plank variant blocked every recipe taking `#planks` (sticks, crafting tables, chests), and a Blocked first match left the output empty even when an unblocked recipe matched.

## Consequences

- The recipe-blocking GameTests on `26.1` and `main` disagree on purpose until issue #26 backports the rule to 1.21.1.
