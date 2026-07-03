# DayZ-Style Inventory Mod for Minecraft (Fabric 1.20.1)

[![License](https://img.shields.io/github/license/aacanadaa/DayZ-Inventory)](LICENSE)
[![Platform](https://img.shields.io/modrinth/game-versions/dayz-inventory)](https://modrinth.com/project/dayz-inventory)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory)](https://modrinth.com/project/dayz-inventory)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory)](https://github.com/aacanadaa/DayZ-Inventory/issues)

A complete overhaul of the Minecraft inventory UI, bringing the look, feel, and mechanics of the DayZ inventory system into Minecraft.

---
<img width="3760" height="1602" alt="image" src="https://github.com/user-attachments/assets/ac38a122-5dbd-4271-b2b3-89e365c476bf" />

## Features

### Unified Vicinity Grid and Expandable Drawers
- **Proximity Scanner**: Scans for ground items and container blocks (chests, barrels, shulker boxes) within a 3-block radius.
- **Unified Column**: Combines nearby ground items and storage blocks into a single scrollable grid under the VICINITY header.
- **Nearby Storage Selectors**: Displays blocks as slot icons with coordinate hover tooltips and distance measurements.
- **Inline Drawer Grids**: Click container selectors to toggle slide-out slot drawers directly inline below the vicinity items, marked by dynamic overlay chevrons (^ and v).

### Dynamic Hands Attachment Slot
- **Active Hotbar Binding**: Dynamically mirrors and replicates whichever hotbar slot is currently active/selected on the player's HUD.
- **Large Attachment Slot**: Replaces the default slot layout with a fully translucent panel body. Hovering anywhere over the panel highlights the entire Hands section.
- **Double-Scaled Render**: Items held in your hands are rendered at 2.0x scale (32x32 pixels) centered inside the panel's free space.
- **Capitalized Item Name Banner**: Displays a sliding banner showing the item name (e.g., HUNTING KNIFE) directly underneath the header.

### Integrated Crafting Menu
- **Vanilla 2x2 Grid**: Integrates the standard 2x2 crafting container and result slot directly between the player's 27-slot inventory grid and the 9-slot hotbar row.
- **Recipe Matching**: Connected to the server-side recipe registry to validate matching recipes and update results in real-time.
- **Item Return**: Safely returns remaining crafting inputs to the player's inventory or drops them when the screen is closed.

### Equipment Drag-and-Drop and Auto-Swapping
- **Drag-to-Equip**: Drag clothing or armor directly onto the middle Survivor panel to automatically equip or swap them.
- **Item Swapping**: Dragging a vicinity item onto an occupied slot (e.g. dragging an iron chestplate onto an equipped diamond chestplate) automatically equips the new item and returns the old one to the player's inventory.
- **True Click-Hold-Drag-Release**: Supports intuitive clicking and dragging for all item management tasks.

### Cursor Jump Prevention
- Caches raw cursor positions before opening or collapsing nearby containers, restoring the cursor position via GLFW on the next frame so the mouse never resets to the center.

---

## Technical Details and Architecture

### Split Client-Server Packets
All vicinity interactions use network packets registered in DayZInventoryPackets.java:
- `OPEN_CONTAINER_PACKET`: Requests opening a nearby chest coordinate selector.
- `PICKUP_ITEM_PACKET`: Handles dragging vicinity entities directly into specific slot indices (including armor slots).
- `QUICK_PICKUP_ITEM_PACKET`: Quick shift-click pickups for vicinity items.

### Access-Safe Client Mixins
- **AbstractContainerScreenMixin.java**: Injects into standard `isHovering` slot checks to support double-size hitboxes for the mirrored Hands attachment slot.

---

## Getting Started

### Requirements
- **Java 17** (or higher)
- **Gradle** (included wrapper)

### Setup and Compilation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/aacanadaa/DayZ-Inventory.git
   cd DayZ-Inventory
   ```

2. **Build the Mod Jar:**
   ```bash
   ./gradlew build
   ```
   The compiled jar will be generated inside `build/libs/`.

3. **Run the Client (Development):**
   ```bash
   ./gradlew runClient
   ```

---

## License and Credits
- **Author**: suoim (Modrinth Profile: https://modrinth.com/user/suoim)
- **License**: Apache-2.0 (see LICENSE)
