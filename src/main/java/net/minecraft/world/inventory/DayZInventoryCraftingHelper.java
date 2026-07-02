package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DayZInventoryCraftingHelper {
    public static void updateCraftingGrid(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftSlots, ResultContainer resultSlots) {
        CraftingMenu.slotChangedCraftingGrid(menu, level, player, craftSlots, resultSlots);
    }
}
