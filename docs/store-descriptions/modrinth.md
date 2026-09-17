# DayZ Inventory

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

**Really recommended with [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** — seamless integration. The two are built as a pair, so the HUD and the inventory screen share one look.

**Now built from one source tree for 23 Minecraft versions on Fabric, NeoForge and Forge.**

| Minecraft | Fabric | NeoForge | Forge | Java |
| :--- | :---: | :---: | :---: | :--- |
| **1.20.1** | ✅ | — | — | 17 |
| **1.20.2** | ✅ | — | — | 17 |
| **1.20.3** | ✅ | — | — | 17 |
| **1.20.4** | ✅ | — | — | 17 |
| **1.20.5** | ✅ | — | — | 21 |
| **1.20.6** | ✅ | ✅ | ✅ | 21 |
| **1.21** | ✅ | ✅ | ✅ | 21 |
| **1.21.1** | ✅ | ✅ | ✅ | 21 |
| **1.21.2** | ✅ | ✅ | — | 21 |
| **1.21.3** | ✅ | ✅ | ✅ | 21 |
| **1.21.4** | ✅ | ✅ | ✅ | 21 |
| **1.21.5** | ✅ | ✅ | ✅ | 21 |
| **1.21.6** | ✅ | ✅ | ✅ | 21 |
| **1.21.7** | ✅ | ✅ | ✅ | 21 |
| **1.21.8** | ✅ | ✅ | ✅ | 21 |
| **1.21.9** | ✅ | ✅ | ✅ | 21 |
| **1.21.10** | ✅ | ✅ | ✅ | 21 |
| **1.21.11** | ✅ | ✅ | ✅ | 21 |
| **26.1** | ✅ | ✅ | — | 25 |
| **26.1.1** | ✅ | ✅ | — | 25 |
| **26.1.2** | ✅ | ✅ | — | 25 |
| **26.2** | ✅ | ✅ | — | 25 |
| **26.3** | ✅ | ✅ | — | 25 |

**Gaps.** 1.20.1 through 1.20.4 are Fabric-only: 1.20.1 predates NeoForge, NeoForge's 1.20.2 build
still used an older networking API this mod does not carry, 1.20.3 never had a NeoForge release, 1.20.4
predates the payload system the mod uses, and Forge 1.20.1 is not buildable with the current toolchain.
1.20.5 is Fabric-only too — NeoForge's release for it is not usable with this build, and Forge has no
1.20.5 release. 1.21.2 has no Forge file because Forge skipped that release, and the 26.x line has no
Forge file either. Otherwise, Forge is available for 1.20.6 through 1.21.11, and Fabric and NeoForge
together cover every version from 1.20.6 up to 26.3 — 53 downloads in total (23 Fabric, 18 NeoForge,
12 Forge).

![DayZ Inventory UI — Vicinity grid with an open Jukebox drawer, the Survivor panel, the 2.0x Hands slot showing a Decorated Pot, and the 2x2 crafting grid](https://raw.githubusercontent.com/aacanadaa/DayZ-Inventory/main/docs/screenshots/ui-example.png)

---

## Features

### Unified Vicinity Grid and Expandable Drawers

- **Proximity Scanner** — Scans for ground items and container blocks (chests, barrels, shulker boxes) within a 3-block radius.
- **Unified Column** — Combines nearby ground items and storage blocks into a single scrollable grid under the VICINITY header.
- **Nearby Storage Selectors** — Displays blocks as slot icons with coordinate hover tooltips and distance measurements.
- **Inline Drawer Grids** — Click container selectors to toggle slide-out slot drawers directly inline below the vicinity items.

### Dynamic Hands Attachment Slot

- **Active Hotbar Binding** — Dynamically mirrors whichever hotbar slot is currently active on the player's HUD.
- **Large Attachment Slot** — Replaces the default slot layout with a fully translucent panel body.
- **Double-Scaled Render** — Items held in your hands are rendered at 2.0x scale (32x32 pixels) centered inside the panel.
- **Item Name Banner** — Displays a sliding banner showing the item name (e.g. HUNTING KNIFE) underneath the header.

### Integrated Crafting Menu and Equipment Swapping

- **Vanilla 2x2 Grid** — Standard 2x2 crafting container and result slot integrated directly into the UI.
- **Drag-to-Equip** — Drag clothing or armor onto the middle Survivor panel to automatically equip or swap it.

### Manual Pickup

Items are no longer picked up by walking over them. Loot is collected deliberately from the Vicinity grid.

### Optional Mod Integration

- **Recipe Viewers (JEI / REI / EMI)** — Adds a theme-aligned toggle button to the header when one is installed.
- **Curios API** — Adds a CURIOS button to the Survivor header when Curios is present.
- **Trinkets** — Adds a TRINKETS button to the Survivor header when Trinkets is present (Fabric).

Every optional integration is probed at runtime. The mod never requires them and will not crash on launch or when opening the inventory if they are absent.

---

## Installation

1. Install the loader you want for your Minecraft version — **Fabric**, **NeoForge** or **Forge**. The table above shows which loaders are available for each Minecraft version.
2. Download the file matching your Minecraft version and loader from the **Versions** tab. Every Minecraft version in the table above is supported, and each download is labelled with its Minecraft version and loader.
3. Drop it into your `mods/` folder.

**Pick carefully.** A Fabric build will not load on NeoForge or Forge, and a build for one Minecraft version will not load on another. Each download is labelled with its version and loader.

---

## Dependencies

Pick the row for your loader. Optional entries are probed at runtime — the mod never requires them and will not crash if they are absent.

| Loader | Minecraft | Java | Required | Optional |
| :--- | :--- | :--- | :--- | :--- |
| **Fabric** | Every version from 1.20.1 to 26.3 | 17 on 1.20.1 – 1.20.4 · 21 on 1.20.5 – 1.21.11 · 25 on 26.x | Fabric Loader 0.16.14+ (0.19.5+ from 1.21.5) and the Fabric API build for your Minecraft version | JEI / REI / EMI, Trinkets, Curios |
| **NeoForge** | Every version from 1.20.6 to 26.3 | 21 on 1.21.x · 25 on 26.x | NeoForge for your Minecraft version | JEI, Curios |
| **Forge** | 1.20.6 through 1.21.11 | 21 | Forge for your Minecraft version | JEI, Curios |

Newest builds: Fabric Loader 0.19.5 with Fabric API 0.160.6+26.3 on 26.3; NeoForge 26.3.0.1-beta on 26.3; Forge 61.2.1 on 1.21.11. Older Minecraft versions follow the same pattern — use the loader and API build that matches your Minecraft version.

Trinkets is Fabric-only. NeoForge and Forge need no additional libraries — they ship their own loader and networking.
Minecraft 26.x requires **Java 25**; 1.21.x requires **Java 21**; 1.20.1 requires **Java 17**.

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
