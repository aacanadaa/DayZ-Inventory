# DayZ Inventory

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=CurseForge%20Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

| Minecraft | Loaders |
| :--- | :--- |
| **1.21.11** | Fabric · NeoForge |
| **1.21.1** | Fabric · NeoForge |
| **1.20.1** | Fabric · Forge |

![The DayZ Inventory screen: a VICINITY grid of nearby items, a CHEST drawer open with shells, the SURVIVOR panel with the player in gear, CURIOS and JEI buttons in the header, an M1014 Battle Shotgun in the 2.0x HANDS slot, and the 2x2 CRAFTING grid](docs/screenshots/ui-example.png)

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

1. Install the Minecraft version you want:
   - **1.21.11** with **Fabric Loader** or **NeoForge**
   - **1.21.1** with **Fabric Loader** or **NeoForge**
   - **1.20.1** with **Fabric Loader** or **Forge**
2. Download the file matching your Minecraft version and loader from [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
3. Drop it into your `mods/` folder.

> **Pick carefully.** A Fabric build will not load on Forge or NeoForge, a 1.20.1 build will not load on 1.21.x, and vice versa. Each download is labelled with its version and loader.

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

### Fabric (1.21.11)

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.21.11` |
| **Mandatory** | Fabric Loader | `>=0.15.0` |
| **Mandatory** | Fabric API | Any `1.21.11` build |
| **Mandatory** | Java | `21` |
| *Optional* | JEI / REI / EMI | Any `1.21.11` build |
| *Optional* | Trinkets | Any `1.21.11` build |
| *Optional* | Curios API | Any `1.21.11` build |

### NeoForge (1.21.11)

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.21.11` |
| **Mandatory** | NeoForge | `>=21.11` |
| **Mandatory** | Java | `21` |
| *Optional* | JEI | Any `1.21.11` build |
| *Optional* | Curios API | Any `1.21.11` build |

### Fabric (1.21.1)

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.21.1` |
| **Mandatory** | Fabric Loader | `>=0.15.0` |
| **Mandatory** | Fabric API | Any `1.21.1` build |
| **Mandatory** | Java | `21` |
| *Optional* | JEI / REI / EMI | Any `1.21.1` build |
| *Optional* | Trinkets | Any `1.21.1` build |
| *Optional* | Curios API | Any `1.21.1` build |

### NeoForge (1.21.1)

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.21.1` |
| **Mandatory** | NeoForge | `>=21.1` |
| **Mandatory** | Java | `21` |
| *Optional* | JEI | Any `1.21.1` build |
| *Optional* | Curios API | Any `1.21.1` build |

> NeoForge needs no additional libraries — it has its own loader and networking built in.

---

## Building from Source

Each Minecraft version lives on its own branch:

| Branch | Minecraft | Loaders | JDK |
| :--- | :--- | :--- | :--- |
| `1.21.11` | 1.21.11 | Fabric, NeoForge | **21** |
| `1.21.1` | 1.21.1 | Fabric, NeoForge | **21** |
| `main` | 1.20.1 | Fabric, Forge | **17** |

```bash
git clone https://github.com/aacanadaa/DayZ-Inventory.git
cd DayZ-Inventory
git checkout 1.21.11   # or 1.21.1, or stay on main for 1.20.1
./gradlew build
```

Output JARs:

| Branch | Loader | Path |
| :--- | :--- | :--- |
| 1.21.11 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<mc>-<version>.jar` |
| 1.21.11 | NeoForge | `neoforge/build/libs/dayz-inventory-neoforge-<mc>-<version>.jar` |
| 1.21.1 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<version>.jar` |
| 1.21.1 | NeoForge | `neoforge/build/libs/dayz-inventory-neoforge-<version>.jar` |
| 1.20.1 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<version>.jar` |
| 1.20.1 | Forge | `forge/build/libs/dayz-inventory-forge-<version>.jar` |

> **Developing on the GUI?** Fabric development runs (`:fabric:runClient`) start the game but do
> not apply mixins, because the project uses Mojang official mappings and Fabric's dev-time mixin
> remapper expects intermediary. The released Fabric jar is unaffected. Build the jar and test it in
> a launcher instead. See `CLAUDE.md` for the full explanation.

---

## Links

- **Modrinth**: <https://modrinth.com/mod/dayz-inventory>
- **CurseForge**: <https://www.curseforge.com/minecraft/mc-mods/dayz-inventory>
- **Versions**: <https://modrinth.com/mod/dayz-inventory/versions> · <https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files>
- **Source Code**: <https://github.com/aacanadaa/DayZ-Inventory>
- **Issue Tracker**: <https://github.com/aacanadaa/DayZ-Inventory/issues>
- **Author**: <https://modrinth.com/user/suoim>

---

## License & Copyright

- **Author**: suoim ([Modrinth Profile](https://modrinth.com/user/suoim))
- **License**: [Apache License 2.0](LICENSE)

Copyright 2026 suoim. Licensed under the Apache License, Version 2.0.
