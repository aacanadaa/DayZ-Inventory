# DayZ Inventory

[中文](README.md) | **English**

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the
DayZ inventory system into Minecraft: a **Vicinity** grid of nearby ground items and containers, a
dynamic **Hands** attachment slot bound to the active hotbar slot, an integrated **2x2 crafting
grid**, and drag-to-equip onto the **Survivor** panel.

**Really recommended with [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** — seamless
integration. The two are built as a pair, so the HUD and the inventory screen share one look.

## Supported versions and loaders

| Minecraft | Fabric | NeoForge | Forge |
| :--- | :---: | :---: | :---: |
| **26.2** | ✅ | ✅ | — |
| **1.21.11** | ✅ | ✅ | — |
| **1.21.1** | ✅ | ✅ | ✅ |
| **1.20.1** | see below | — | see below |

Forge stopped at 1.20.x — the ecosystem moved to NeoForge — which makes 1.21.1 the newest version
Forge can be built for at all.

Every artifact is built from **one source tree**: [Stonecutter](https://stonecutter.kikugie.dev/)
handles multi-version preprocessing and the `common/` + per-loader modules handle loader
abstraction. The matrix is declared in `settings.gradle.kts`; adding a version is one line there plus
a `versions/<mc>/gradle.properties` file.

> **About 1.20.1**
> 1.20.1 is still shipped from its own `1.20.1` branch and has not been folded into the unified tree
> yet. It predates the 1.20.5 networking rewrite — there are no custom payload records, and
> `ExtendedScreenHandlerType` does not take an opening-data codec yet — so it needs a real port
> rather than a rebuild. The build scaffolding (`versions/1.20.1/gradle.properties`) is in place and
> the exact breakpoints are listed in [docs/BUILDING.en.md](docs/BUILDING.en.md).

![The DayZ Inventory screen: a VICINITY grid of nearby items, a CHEST drawer open with shells, the SURVIVOR panel with the player in gear, CURIOS and JEI buttons in the header, an M1014 Battle Shotgun in the 2.0x HANDS slot, and the 2x2 CRAFTING grid](docs/screenshots/ui-example.png)

---

## Features

### Unified Vicinity Grid and Expandable Drawers
- **Proximity Scanner**: rescans every 10 ticks for ground items and container blocks (chests,
  barrels, shulker boxes) within a 3-block radius.
- **Unified Column**: nearby ground items and storage blocks share one scrollable grid under the
  VICINITY header.
- **Container Selectors**: containers are drawn as slot icons with coordinate and distance tooltips.
- **Inline Drawer Grids**: clicking a container selector expands its slots inline, below the grid.

### Dynamic Hands Attachment Slot
- **Active Hotbar Binding**: mirrors whichever hotbar slot is currently selected.
- **Large Attachment Slot**: a fully translucent panel body in place of the default slot.
- **Double-Scaled Render**: the held item is drawn at **2.0x** (32x32 px) centred in the panel.
- **Capitalised Name Banner**: shows the item name (e.g. `HUNTING KNIFE`) under the header.

### Crafting and Equipment Swapping
- **Vanilla 2x2 Grid**: the crafting grid and result slot live inside the custom screen; the result
  is recomputed server-side.
- **Drag-to-Equip**: drag armour or clothing onto the middle Survivor panel to equip or swap it.

### Optional Mod Integration
- **Recipe Viewers (JEI / REI / EMI)**: a theme-aligned toggle button in the header, drawn only when
  a supported viewer is installed.
- **Curios API**: adds a CURIOS button to the Survivor header.
- **Trinkets**: adds a TRINKETS button to the Survivor header.

Every integration is probed at runtime with `isModLoaded`. The mod launches, opens its screen and
works fully with **none** of them installed.

---

## Installation

1. Install **26.2** or **1.21.11** with **Fabric Loader** or **NeoForge**, or **1.21.1** with
   **Fabric Loader**, **NeoForge** or **Forge**.
2. Download the file whose name matches your Minecraft version *and* loader from
   [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) or
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
3. Drop it into `mods/`.

> **Pick carefully.** A Fabric build will not load on Forge or NeoForge, and a 1.21.1 build will not
> load on 1.21.11 or 26.2. Each filename carries its Minecraft version and loader.

## Dependencies

| Minecraft | Loader | Java | Required | Optional |
| :--- | :--- | :--- | :--- | :--- |
| **26.2** | Fabric | 25 | Fabric Loader `>=0.19.5`, Fabric API `0.160.0+26.2` | JEI / REI / EMI, Trinkets, Curios |
| **26.2** | NeoForge | 25 | NeoForge `>=26.2.0.88` | JEI, Curios |
| **1.21.11** | Fabric | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.141.6+1.21.11` | JEI / REI / EMI, Trinkets, Curios |
| **1.21.11** | NeoForge | 21 | NeoForge `>=21.11.45` | JEI, Curios |
| **1.21.1** | Fabric | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.116.17+1.21.1` | JEI / REI / EMI, Trinkets, Curios |
| **1.21.1** | NeoForge | 21 | NeoForge `>=21.1.250` | JEI, Curios |
| **1.21.1** | Forge | 21 | Forge `>=52.1.12` | JEI, Curios |

Optional entries are probed at runtime — the mod never requires them and will not crash without them.

---

## Building from source

The Gradle launcher needs **JDK 25**. The Java 17 / 21 toolchains the rest of the matrix needs are
downloaded automatically by the foojay resolver, so nothing has to be installed by hand.

```bash
# Builds every Minecraft version x every mod loader in the matrix.
./gradlew chiseledBuild

# A single target (artifacts land in <loader>/versions/<mc>/build/libs/).
./gradlew :fabric:26.2:build
./gradlew :neoforge:1.21.11:build
./gradlew :forge:1.21.1:build

# List every node in the matrix.
./gradlew matrix
```

Artifacts are named `dayz-inventory-<loader>-<minecraft>-<mod version>.jar`, for example
`dayz-inventory-fabric-26.2-1.5.0+mc26.2.jar`. The Minecraft version is part of the filename on
purpose: the same mod version ships for several game versions, and CurseForge rejects a second file
whose display name collides inside a project.

Architecture, the conditional-compilation conventions and how to add a version are documented in
**[docs/BUILDING.en.md](docs/BUILDING.en.md)**.

### Publishing to CurseForge and Modrinth

```bash
MODRINTH_TOKEN=... CURSEFORGE_API_KEY=... \
  ./gradlew publishAll -Ppublish.dry_run=false
```

- Every node is tagged with **its own** game version and loader, taken from
  `versions/<mc>/gradle.properties`, so a jar cannot be uploaded under the wrong Minecraft version.
- `publish.dry_run` defaults to `true`; without turning it off, `publishMods` only logs.
- **CurseForge publishes are fire-and-forget.** Every file goes through human review and the API
  accepts it without returning a URL, so a green CurseForge task means "submitted", not "live".
- One platform at a time: `:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`.

CI (`.github/workflows/build.yml`) builds the whole matrix on a `v*` tag, attaches every jar to the
GitHub Release and publishes to both platforms.

## Versioning

`mod.version` in `gradle.properties` is the single source of truth. It is expanded into
`fabric.mod.json`, `neoforge.mods.toml` and `pack.mcmeta`. Bump it, then update
[CHANGELOG.md](CHANGELOG.md).

## License

Apache License 2.0 — see [LICENSE](LICENSE).
