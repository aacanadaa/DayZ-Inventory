# DayZ-Style Inventory Mod for Minecraft (Fabric & Forge 1.20.1)

[![Platform](https://img.shields.io/modrinth/game-versions/dayz-inventory)](https://modrinth.com/project/dayz-inventory)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory)](https://modrinth.com/project/dayz-inventory)
[![License](https://img.shields.io/badge/License-All_Rights_Reserved-red.svg)](LICENSE)

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

---

<img width="3732" height="1668" alt="image" src="https://github.com/user-attachments/assets/c2c2ae16-af85-454d-98df-4340832bc25d" />

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

---

## Dependencies & Platform Requirements

### Fabric (1.20.1)
| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.20.1` |
| **Mandatory** | Fabric Loader | `>=0.14.0` |
| **Mandatory** | Fabric API | Any compatible `1.20.1` build |
| *Optional* | Trinkets | Any compatible `1.20.1` build |
| *Optional* | Curios API | Any compatible `1.20.1` build |

### Forge (1.20.1)
| Type | Dependency | Required Version |
| :--- | :--- | :--- |
| **Mandatory** | Minecraft | `1.20.1` |
| **Mandatory** | Forge | `>=47.0.0` |
| *Optional* | Curios API (Forge) | `>=5.0.0` |

---

## Issue Tracking & Bug Reports
For bug reports, feature requests, or compatibility issues, please use our public issue tracker:
- **Submit an Issue**: [Public Issue Tracker](https://github.com/aacanadaa/DayZ-Inventory-Issues/issues)

---

## License & Copyright
- **Author**: suoim (Modrinth Profile: [https://modrinth.com/user/suoim](https://modrinth.com/user/suoim))
- **License**: All Rights Reserved (ARR). See [LICENSE](LICENSE) file for full details.
