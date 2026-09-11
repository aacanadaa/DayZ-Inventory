# DayZ Inventory

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory)](https://github.com/aacanadaa/DayZ-Inventory/issues)

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft. Available for **Fabric** and **Forge** on **Minecraft 1.20.1**.

![DayZ Inventory UI — Vicinity grid with an open Jukebox drawer, the Survivor panel, the 2.0x Hands slot showing a Decorated Pot, and the 2x2 crafting grid](docs/screenshots/ui-example.png)

---

## Features

### Unified Vicinity Grid and Expandable Drawers
- **Proximity Scanner**: Scans for ground items and container blocks (chests, barrels, shulker boxes) within a 3-block radius.
- **Unified Column**: Combines nearby ground items and storage blocks into a single scrollable grid under the VICINITY header.
- **Nearby Storage Selectors**: Displays blocks as slot icons with coordinate hover tooltips and distance measurements.
- **Inline Drawer Grids**: Click container selectors to toggle slide-out slot drawers directly inline below the vicinity items.

### Dynamic Hands Attachment Slot
- **Active Hotbar Binding**: Dynamically mirrors whichever hotbar slot is currently active/selected on the player's HUD.
- **Large Attachment Slot**: Replaces the default slot layout with a fully translucent panel body.
- **Double-Scaled Render**: Items held in your hands are rendered at 2.0x scale (32x32 pixels) centered inside the panel's free space.
- **Capitalized Item Name Banner**: Displays a sliding banner showing the item name (e.g., HUNTING KNIFE) directly underneath the header.

### Integrated Crafting Menu & Equipment Swapping
- **Vanilla 2x2 Grid**: Integrates standard 2x2 crafting container and result slot directly in the UI.
- **Drag-to-Equip**: Drag clothing or armor directly onto the middle Survivor panel to automatically equip or swap them.

### Optional Mod Integration
- **Recipe Viewers (JEI / REI / EMI)**: Adds a theme-aligned toggle button to the header. Fully optional — the button only appears when a supported recipe viewer is installed.
- **Curios API**: Adds a CURIOS button to the Survivor header when Curios is present.
- **Trinkets**: Adds a TRINKETS button to the Survivor header when Trinkets is present.

All optional integrations are probed at runtime. The mod never requires them and will not crash on launch or on opening the inventory when they are absent.

---

## Installation

1. Install **Minecraft 1.20.1** with either **Fabric Loader** or **Forge**.
2. Download the matching JAR for your loader from [Modrinth](https://modrinth.com/mod/dayz-inventory/versions).
3. Drop it into your `mods/` folder.

> Make sure you pick the correct file. The Fabric build will not load on Forge and vice versa.

---

## Dependencies

### Fabric (1.20.1)

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.20.1` |
| **Mandatory** | Fabric Loader | `>=0.15.0` |
| **Mandatory** | Fabric API | Any `1.20.1` build |
| *Optional* | JEI / REI / EMI | Any `1.20.1` build |
| *Optional* | Trinkets | Any `1.20.1` build |
| *Optional* | Curios API | Any `1.20.1` build |

### Forge (1.20.1)

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.20.1` |
| **Mandatory** | Forge | `>=47.0.0` |
| *Optional* | JEI | Any `1.20.1` build |
| *Optional* | Curios API | `>=5.0.0` |

---

## Building from Source

Requires **JDK 17**.

```bash
git clone https://github.com/aacanadaa/DayZ-Inventory.git
cd DayZ-Inventory
./gradlew build
```

Output JARs:

| Loader | Path |
| :--- | :--- |
| Fabric | `fabric/build/libs/dayz-inventory-fabric-<version>.jar` |
| Forge | `forge/build/libs/dayz-inventory-forge-<version>.jar` |

> **Developing on the GUI?** Use `./gradlew :forge:runClient`. Fabric development runs
> (`:fabric:runClient`) start the game but do not apply mixins, because the project uses Mojang
> official mappings and Fabric's dev-time mixin remapper expects intermediary. The released Fabric
> jar is unaffected. See `CLAUDE.md` for the full explanation.

---

## Links

- **Modrinth**: <https://modrinth.com/mod/dayz-inventory>
- **Versions**: <https://modrinth.com/mod/dayz-inventory/versions>
- **Source Code**: <https://github.com/aacanadaa/DayZ-Inventory>
- **Issue Tracker**: <https://github.com/aacanadaa/DayZ-Inventory/issues>
- **Author**: <https://modrinth.com/user/suoim>

---

## License & Copyright

- **Author**: suoim ([Modrinth Profile](https://modrinth.com/user/suoim))
- **License**: [Apache License 2.0](LICENSE)

Copyright 2026 suoim. Licensed under the Apache License, Version 2.0.
