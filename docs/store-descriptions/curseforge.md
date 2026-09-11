<!--
CurseForge project description for DayZ Inventory.

CurseForge has no API available to the upload token used by the build, so this
file is the source of truth to copy from when updating the project page:

  https://www.curseforge.com/minecraft/mc-mods/dayz-inventory

Paste this content into the project description editor. Keep it in sync with
README.md when features change.
-->

# DayZ Inventory

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

**Fabric and Forge — Minecraft 1.20.1.** Forge works natively, with no Sinytra Connector required.

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
- **Trinkets** — Adds a TRINKETS button to the Survivor header when Trinkets is present.

Every optional integration is probed at runtime. The mod never requires them and will not crash on launch or when opening the inventory if they are absent.

---

## Installation

1. Install **Minecraft 1.20.1** with either **Fabric Loader** or **Forge**.
2. Download the file matching your loader from the **Files** tab.
3. Drop it into your `mods/` folder.

**Make sure you pick the correct file.** The Fabric build will not load on Forge and vice versa.

---

## Dependencies

### Fabric

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | 1.20.1 |
| **Mandatory** | Fabric Loader | 0.15.0 or newer |
| **Mandatory** | Fabric API | Any 1.20.1 build |
| *Optional* | JEI / REI / EMI | Any 1.20.1 build |
| *Optional* | Trinkets | Any 1.20.1 build |
| *Optional* | Curios API | Any 1.20.1 build |

### Forge

| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | 1.20.1 |
| **Mandatory** | Forge | 47.0.0 or newer |
| *Optional* | JEI | Any 1.20.1 build |
| *Optional* | Curios API | 5.0.0 or newer |

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
