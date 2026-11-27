# DayZ Inventory

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

**Really recommended with [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** — seamless integration. The two are built as a pair, so the HUD and the inventory screen share one look.

**23 Minecraft versions — 1.20.1 through 26.3 — on Fabric, NeoForge and Forge, all from one source tree.**

| Loader | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

54 files in total. Each file is labelled with its Minecraft version and its loader.

![DayZ Inventory UI — Vicinity grid with an open chest drawer, the Survivor panel, the 2.0x Hands slot, and the 2x2 crafting grid](https://raw.githubusercontent.com/aacanadaa/DayZ-Inventory/main/docs/screenshots/ui-example.png)

---

## Features

### Vicinity

- **Ground items and containers in one grid** — dropped items, chests, barrels, shulker boxes and any other block with an inventory within 3 blocks (configurable), nearest container first.
- **Pick up by hand** — walk-over pickup is off. Drag an item into a slot, drop it on the Survivor panel to equip it, or shift-click it to take it.
- **Inline drawers** — click a container to open its slots below the grid, sort it, and close it again. Locks, blocked chests and loot tables work as when you right-click the block.

### Survivor

- **Gear** — armour and offhand slots, with drag-to-equip onto the panel.
- **Status effects** — icons with name, level and time left on hover.
- **Vitals** — health, food and thirst bars. Thirst shows with **Tough As Nails**, **Thirst Was Taken** or **Dehydration**.
- **Hands** — the held item at 2x, bound to the selected hotbar slot.

### Inventory

- **Sort** — merges partial stacks and sorts the main inventory; the hotbar stays put.
- **Shift-click** between the main inventory and the hotbar, into an open container, or onto your body.
- **2x2 crafting**, computed on the server like vanilla's.

### Works with

- **JEI / REI / EMI** — a button that shows or hides the item list.
- **Curios / Accessories / Trinkets** — a button for each one installed.
- **Mouse Tweaks** — its drags and scroll-wheel moves work in the DayZ screen.

All optional: the mod works fully without any of them.

### Configuration

`config/dayz_inventory.properties` lets you turn walk-over pickup back on, keep the vanilla screen for chests or the inventory key, change the Vicinity radius, and hide the effects or vitals.

---

## Installation

1. Install **Fabric**, **NeoForge** or **Forge** for the Minecraft version you play.
2. Download the file matching that Minecraft version and loader from the **Versions** tab.
3. Drop it into your `mods/` folder — on the server and on every client.

**Pick carefully.** A Fabric build will not load on NeoForge or Forge, and a build for one Minecraft version will not load on another.

---

## Requirements

| Loader | Required |
| :--- | :--- |
| **Fabric** | Fabric Loader 0.15+ and the Fabric API build for your Minecraft version |
| **NeoForge** | NeoForge for your Minecraft version |
| **Forge** | Forge for your Minecraft version (1.20.1: any 47.x) |

Java 17 on 1.20.1 – 1.20.4, Java 21 on 1.20.5 – 1.21.11, Java 25 on 26.x.

---

## Links

- **Source Code** — https://github.com/aacanadaa/DayZ-Inventory
- **Report a Bug or Request a Feature** — https://github.com/aacanadaa/DayZ-Inventory/issues
- **Changelog** — https://github.com/aacanadaa/DayZ-Inventory/blob/main/CHANGELOG.md
- **CurseForge** — https://www.curseforge.com/minecraft/mc-mods/dayz-inventory
- **DayZ Hotbar** — https://modrinth.com/mod/dayz-hotbar

---

## License

Licensed under the **Apache License 2.0**. You are free to use, modify, redistribute and include this mod in modpacks.

Copyright 2026 suoim.
