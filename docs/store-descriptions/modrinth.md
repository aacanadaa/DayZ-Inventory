# DayZ Inventory

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

**Really recommended with [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** — seamless integration. The two are built as a pair, so the HUD and the inventory screen share one look.

**23 Minecraft versions — 1.20.1 through 26.3 — on Fabric, NeoForge and Forge, all from one source tree.**

| Loader | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.6 – 1.21.11 |

53 downloads in total. Each file is labelled with its Minecraft version and its loader.

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

1. Install **Fabric**, **NeoForge** or **Forge** for the Minecraft version you play.
2. Download the file matching that Minecraft version and loader from the **Versions** tab.
3. Drop it into your `mods/` folder.

**Pick carefully.** A Fabric build will not load on NeoForge or Forge, and a build for one Minecraft version will not load on another. Each download is labelled with its version and loader.

---

## Dependencies

| Loader | Required |
| :--- | :--- |
| **Fabric** | Fabric Loader 0.16.14+ (0.19.5+ from 1.21.5) and the Fabric API build for your Minecraft version |
| **NeoForge** | NeoForge for your Minecraft version |
| **Forge** | Forge for your Minecraft version |

Java 17 on 1.20.1 – 1.20.4, Java 21 on 1.20.5 – 1.21.11, Java 25 on 26.x.

Optional: **JEI**, **REI** or **EMI** adds a recipe-viewer button, and **Curios** (NeoForge/Forge) or
**Trinkets** (Fabric) adds an accessories button. All of them are probed at runtime — the mod runs
fine without any of them.

1.20.1 – 1.20.5 are Fabric-only, and Forge ends at 1.21.11: those combinations either never had a
usable loader release or predate the networking API this mod uses.

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
