# DayZ Inventory

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

**Really recommended with [DayZ Hotbar](https://www.curseforge.com/minecraft/mc-mods/dayz-hotbar)** — seamless integration. The two are built as a pair, so the HUD and the inventory screen share one look.

**Now available for Minecraft 26.2, 1.21.11, 1.21.1 and 1.20.1.**

| Minecraft | Loaders |
| :--- | :--- |
| **26.2** | Fabric · NeoForge (Java 25) |
| **1.21.11** | Fabric · NeoForge (Java 21) |
| **1.21.1** | Fabric · NeoForge (Java 21) |
| **1.20.1** | Fabric · Forge (Java 17) |

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

1. Install the Minecraft version you want:
   - **26.2** with **Fabric Loader** or **NeoForge**
   - **1.21.11** with **Fabric Loader** or **NeoForge**
   - **1.21.1** with **Fabric Loader** or **NeoForge**
   - **1.20.1** with **Fabric Loader** or **Forge**
2. Download the file matching your Minecraft version and loader from the **Files** tab.
3. Drop it into your `mods/` folder.

**Pick carefully.** A Fabric build will not load on Forge or NeoForge, a 1.20.1 build will not load on 1.21.x or 26.x, and vice versa. Each download is labelled with its version and loader.

---

## Dependencies

Pick the row for your Minecraft version and loader. Optional entries are probed at runtime — the mod never requires them and will not crash if they are absent.

| Minecraft | Loader | Java | Required | Optional |
| :--- | :--- | :--- | :--- | :--- |
| **26.2** | Fabric | 25 | Fabric Loader 0.19.3+, Fabric API 0.160.0+26.2 | JEI / REI / EMI, Trinkets, Curios |
| **26.2** | NeoForge | 25 | NeoForge 26.2+ | JEI, Curios |
| **1.21.11** | Fabric | 21 | Fabric Loader 0.15.0+, Fabric API | JEI / REI / EMI, Trinkets, Curios |
| **1.21.11** | NeoForge | 21 | NeoForge 21.11+ | JEI, Curios |
| **1.21.1** | Fabric | 21 | Fabric Loader 0.15.0+, Fabric API | JEI / REI / EMI, Trinkets, Curios |
| **1.21.1** | NeoForge | 21 | NeoForge 21.1+ | JEI, Curios |
| **1.20.1** | Fabric | 17 | Fabric Loader 0.15.0+, Fabric API | JEI / REI / EMI, Trinkets, Curios |
| **1.20.1** | Forge | 17 | Forge 47.0.0+ | JEI, Curios 5.0.0+ |

Trinkets is Fabric-only. NeoForge needs no additional libraries — it has its own loader and networking built in.

---

## Links

- **Source Code** — https://github.com/aacanadaa/DayZ-Inventory
- **Report a Bug or Request a Feature** — https://github.com/aacanadaa/DayZ-Inventory/issues
- **Changelog** — https://github.com/aacanadaa/DayZ-Inventory/blob/main/CHANGELOG.md
- **Modrinth** — https://modrinth.com/mod/dayz-inventory

---

## License

Licensed under the **Apache License 2.0**. You are free to use, modify, redistribute and include this mod in modpacks.

Copyright 2026 suoim.
