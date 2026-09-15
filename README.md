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

**Really recommended with [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** — seamless integration. The two are built as a pair, so the HUD and the inventory screen share one look.

| Minecraft | Loaders |
| :--- | :--- |
| **26.2** | Fabric · NeoForge |
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
   - **26.2** with **Fabric Loader** or **NeoForge**
   - **1.21.11** with **Fabric Loader** or **NeoForge**
   - **1.21.1** with **Fabric Loader** or **NeoForge**
   - **1.20.1** with **Fabric Loader** or **Forge**
2. Download the file matching your Minecraft version and loader from [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
3. Drop it into your `mods/` folder.

> **Pick carefully.** A Fabric build will not load on Forge or NeoForge, a 1.20.1 build will not load on 1.21.x or 26.x, and vice versa. Each download is labelled with its version and loader.

---

## Dependencies

Pick the row for your Minecraft version and loader. Optional entries are probed at
runtime — the mod never requires them and will not crash if they are absent.

| Minecraft | Loader | Java | Required | Optional |
| :--- | :--- | :--- | :--- | :--- |
| **26.2** | Fabric | 25 | Fabric Loader `>=0.19.3`, Fabric API `0.160.0+26.2` | JEI / REI / EMI, Trinkets, Curios |
| **26.2** | NeoForge | 25 | NeoForge `>=26.2` | JEI, Curios |
| **1.21.11** | Fabric | 21 | Fabric Loader `>=0.15.0`, Fabric API | JEI / REI / EMI, Trinkets, Curios |
| **1.21.11** | NeoForge | 21 | NeoForge `>=21.11` | JEI, Curios |
| **1.21.1** | Fabric | 21 | Fabric Loader `>=0.15.0`, Fabric API | JEI / REI / EMI, Trinkets, Curios |
| **1.21.1** | NeoForge | 21 | NeoForge `>=21.1` | JEI, Curios |
| **1.20.1** | Fabric | 17 | Fabric Loader `>=0.15.0`, Fabric API | JEI / REI / EMI, Trinkets, Curios |
| **1.20.1** | Forge | 17 | Forge `>=47.0.0` | JEI, Curios `>=5.0.0` |

Trinkets is Fabric-only. NeoForge needs no additional libraries — it has its own loader
and networking built in.

---

## Building from Source

Each Minecraft version lives on its own branch:

| Branch | Minecraft | Loaders | JDK |
| :--- | :--- | :--- | :--- |
| `26.2` | 26.2 | Fabric, NeoForge | **25** |
| `1.21.11` | 1.21.11 | Fabric, NeoForge | **21** |
| `1.21.1` | 1.21.1 | Fabric, NeoForge | **21** |
| `main` | 1.20.1 | Fabric, Forge | **17** |

```bash
git clone https://github.com/aacanadaa/DayZ-Inventory.git
cd DayZ-Inventory
git checkout 26.2   # or 1.21.11, 1.21.1, or stay on main for 1.20.1
./gradlew build
```

Output JARs:

| Branch | Loader | Path |
| :--- | :--- | :--- |
| 26.2 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<mc>-<version>.jar` |
| 26.2 | NeoForge | `neoforge/build/libs/dayz-inventory-neoforge-<mc>-<version>.jar` |
| 1.21.11 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<mc>-<version>.jar` |
| 1.21.11 | NeoForge | `neoforge/build/libs/dayz-inventory-neoforge-<mc>-<version>.jar` |
| 1.21.1 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<version>.jar` |
| 1.21.1 | NeoForge | `neoforge/build/libs/dayz-inventory-neoforge-<version>.jar` |
| 1.20.1 | Fabric | `fabric/build/libs/dayz-inventory-fabric-<version>.jar` |
| 1.20.1 | Forge | `forge/build/libs/dayz-inventory-forge-<version>.jar` |

> **Developing on the GUI?** On `1.20.1`, `1.21.1` and `1.21.11`, Fabric development runs
> (`:fabric:runClient`) start the game but do **not** apply mixins, because those branches use Mojang
> official mappings and Fabric's dev-time mixin remapper expects intermediary. The released Fabric jar
> is unaffected — build the jar and test it in a launcher instead. See `CLAUDE.md`.
>
> This does **not** apply on `26.2`. Minecraft has shipped unobfuscated since 26.1, so there are no
> mappings and no refmap, and `:fabric:runClient` applies mixins normally.

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
