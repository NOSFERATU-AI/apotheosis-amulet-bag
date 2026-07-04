# Apotheosis Amulet Bag

**Apotheosis Amulet Bag** is a small NeoForge addon for Minecraft 1.21.1 that adds a dedicated bag for Apotheosis amulets.

The bag is designed to keep several Apotheosis charms in one item while still allowing their effects to work from inside the bag.

## Features

- Adds an **Amulet Bag** item
- Supports up to **5 amulet slots**
- Slots are unlocked with player experience levels:
  - Slot 1: 100 levels
  - Slot 2: 200 levels
  - Slot 3: 300 levels
  - Slot 4: 400 levels
  - Slot 5: 500 levels
- Only Apotheosis amulets can be placed inside
- Charm effects remain active while stored in unlocked slots
- Locked slots are shown visually in the bag GUI
## Notes

This mod was made for servers and modpacks where Apotheosis charms are commonly used and players need a cleaner way to manage them.

The mod is intentionally small and focused: it only adds the amulet bag and does not change Apotheosis progression or charm mechanics directly.

## Controls

- **Right Click** — open the Amulet Bag
- **Shift + Right Click** — unlock the next slot if you have enough levels

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1+**
- Apotheosis

## Item ID 

```mcfunction
/give @p apoth_talisman_bag:talisman_bag
