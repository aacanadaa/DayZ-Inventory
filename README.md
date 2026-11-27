# DayZ Inventory

**English** · [中文](README.zh.md) · [Français](README.fr.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%E2%80%93%2026.3-62b47a.svg)](https://modrinth.com/mod/dayz-inventory/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory?color=red)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Replaces Minecraft's inventory screen with a DayZ-style layout: a **Vicinity** grid for nearby ground items and containers, a **Survivor** panel with your gear, status effects, and vitals, a **2x Hands** slot, and a **2x2 crafting grid**.

Pairs well with [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar), which uses the same visual style for the HUD.

![DayZ Inventory screen](docs/screenshots/ui-example.png)

---

## Supported Versions

| Loader | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

23 Minecraft versions, 54 builds. Download the jar matching both your game version and mod loader (see [Full version table](#full-version-table)).

---

## Features

### Vicinity
- **Ground items & containers:** Shows dropped items and nearby chests, barrels, shulker boxes, and other containers within 3 blocks (configurable), nearest container first.
- **Manual pickup:** Walk-over pickup is disabled in Survival. Drag items into slots, drop gear onto the Survivor panel, or shift-click to loot.
- **Inline drawers:** Click a container to open its slots right under the Vicinity grid, sort it, and close it with `x`. Container locks, blocked lids, and loot tables work just like right-clicking in-world.

### Survivor & Inventory
- **Gear, effects & vitals:** Armor and offhand slots, status effect icons with hover tooltips (blinking in the last 10 seconds), plus health, food, and thirst bars (when **Tough As Nails**, **Thirst Was Taken**, or **Dehydration** is installed).
- **Hands & crafting:** 2x held-item preview bound to your active hotbar slot, plus a 2x2 crafting grid.
- **Sorting & shift-click:** Sort button merges partial stacks and sorts the main inventory (leaving the hotbar alone) or open containers.

### Mod Compatibility (all optional)
- **JEI / REI / EMI:** Toggle button in the header to show or hide the recipe list.
- **Curios / Accessories / Trinkets:** Quick-open button on the Survivor panel when installed.
- **Mouse Tweaks:** Dragging and scroll-wheel item moves work inside the DayZ screen.

---

## Installation

1. Download the jar matching your Minecraft version and loader from [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
2. Put it in `mods/` on **both the client and server**.
3. On **Fabric**, also install [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric Loader 0.15+).

---

## Configuration

`config/dayz_inventory.properties` is created on first launch (restarting applies changes):

| Option | Default | Description |
| :--- | :--- | :--- |
| `autoPickup` | `false` | Walk-over pickup like vanilla (Creative always keeps it) |
| `openContainersInDayZScreen` | `true` | Right-clicking a chest or barrel opens the DayZ screen |
| `vicinityRadius` | `3` | Vicinity scan radius in blocks (1–8; match server & client) |
| `replaceInventoryScreen` | `true` | Inventory key opens the DayZ screen |
| `showStatusEffects` | `true` | Show status effect icons on the Survivor panel |
| `showVitals` | `true` | Show health, food, and thirst bars on the Survivor panel |
| `dragAndDrop` | `true` | Click-and-release slot moves (disabled when Mouse Tweaks is present) |

On dedicated servers, `autoPickup`, `openContainersInDayZScreen`, and `vicinityRadius` are read from the server's config.

---

## Full version table

| Minecraft | Fabric | NeoForge (min.) | Forge (min.) | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.20.1** | ✅ | — | ✅ any 47.x | 17 |
| **1.20.2** | ✅ | — | — | 17 |
| **1.20.3** | ✅ | — | — | 17 |
| **1.20.4** | ✅ | — | — | 17 |
| **1.20.5** | ✅ | — | — | 21 |
| **1.20.6** | ✅ | ✅ 20.6.141+ | ✅ 50.2.10+ | 21 |
| **1.21** | ✅ | ✅ 21.0.167+ | ✅ 51.0.33+ | 21 |
| **1.21.1** | ✅ | ✅ 21.1.250+ | ✅ 52.1.12+ | 21 |
| **1.21.2** | ✅ | ✅ 21.2.1-beta+ | — | 21 |
| **1.21.3** | ✅ | ✅ 21.3.97+ | ✅ 53.1.12+ | 21 |
| **1.21.4** | ✅ | ✅ 21.4.157+ | ✅ 54.1.18+ | 21 |
| **1.21.5** | ✅ | ✅ 21.5.98+ | ✅ 55.1.13+ | 21 |
| **1.21.6** | ✅ | ✅ 21.6.20-beta+ | ✅ 56.0.9+ | 21 |
| **1.21.7** | ✅ | ✅ 21.7.25-beta+ | ✅ 57.0.3+ | 21 |
| **1.21.8** | ✅ | ✅ 21.8.54+ | ✅ 58.1.22+ | 21 |
| **1.21.9** | ✅ | ✅ 21.9.16-beta+ | ✅ 59.0.5+ | 21 |
| **1.21.10** | ✅ | ✅ 21.10.64+ | ✅ 60.1.15+ | 21 |
| **1.21.11** | ✅ | ✅ 21.11.45+ | ✅ 61.2.1+ | 21 |
| **26.1** | ✅ | ✅ 26.1.0.19-beta+ | — | 25 |
| **26.1.1** | ✅ | ✅ 26.1.1.15-beta+ | — | 25 |
| **26.1.2** | ✅ | ✅ 26.1.2.109+ | — | 25 |
| **26.2** | ✅ | ✅ 26.2.0.88+ | — | 25 |
| **26.3** | ✅ | ✅ 26.3.0.1-beta+ | — | 25 |

---

## Building from Source

Requires **JDK 25** as the Gradle launcher (Java 17/21 toolchains download automatically). See [docs/BUILDING.md](docs/BUILDING.md) for details.

```bash
./gradlew chiseledBuild                      # Build all versions and loaders
./gradlew buildMods -Pdayz.versions=1.21.1   # Build one MC version
./gradlew :forge:1.20.1:build                # Build one specific target
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
