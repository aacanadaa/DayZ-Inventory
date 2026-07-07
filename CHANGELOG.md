DayZ Inventory 1.2.0: Accessory Integration and Input Polish Update

Welcome to the official 1.2.0 release of DayZ Inventory! This version adds comprehensive support for the Trinkets accessory mod, improves the layout structure when multiple accessory mods are installed, and refactors the recipe viewer toggle button.

Key Features and Improvements:
- Trinkets Mod Compatibility:
  - Automatically detects if the Trinkets mod is loaded in the current game instance.
  - Adds a flat, theme-aligned TRINKETS button in the header of the middle (Survivor) column.
  - When clicked, the mod temporarily bypasses the inventory screen redirection to open the vanilla InventoryScreen, where all of the Trinket slots/groups are fully initialized and rendered by the Trinkets mod itself.
  - Closing the vanilla screen returns the player back to normal play, and reopening the inventory brings them back to the custom DayZ style layout. This zero-dependency design ensures robust, future-proof compatibility across different versions of the Trinkets API.
- Smart Dynamic Accessory Headers:
  - When both Curios and Trinkets mods are active, they are positioned side-by-side in the Survivor header (CURIOS on the left, TRINKETS on the right) so they never overlap.
  - If only one of the mods is installed, its button is aligned to the right, maintaining a clean and balanced header layout.
- Simplified JEI Toggle Button:
  - Refactored the recipe viewer button click action to unconditionally simulate a tap of the 'O' key on the client (GLFW keycode 79), which is the standard default key for toggling the recipe viewer overlay (JEI, REI, EMI) on and off.

Compatibility Section:
- Minecraft Version: 1.20.1 (Fabric)
- Java Version: Java 17 and newer
- Mod Compatibility List:
  - Just Enough Items (JEI) - Fully compatible (supports toggling overlay via simulated 'O' keytap)
  - Roughly Enough Items (REI) - Fully compatible (supports toggling overlay via simulated 'O' keytap)
  - Every Item / Everything Mod (EMI) - Fully compatible (supports toggling overlay via simulated 'O' keytap)
  - Curios API - Fully compatible (adds a button in the Survivor header to open the Curios screen)
  - Trinkets - Fully compatible (adds a button in the Survivor header to open the vanilla inventory screen)
