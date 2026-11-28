/*
 * DayZ Inventory
 * Copyright 2026 suoim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.suoim.dayzinventory;

import com.suoim.dayzinventory.config.DayZConfig;
import com.suoim.dayzinventory.grid.GridLayout;
import com.suoim.dayzinventory.grid.ItemSizeRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Item sizes applied to a player's inventory.
 * <p>
 * Only the 27 main inventory slots ({@link Inventory} slots 9 to 35, a 9 x 3
 * grid) use sizes. The hotbar, armour and offhand keep one item per slot. An
 * item lives in the slot of the top-left cell of its footprint; see
 * {@link GridLayout} for the rules of the grid.
 * <p>
 * The server enforces sizes in three places: every {@code Slot} over the main
 * inventory refuses an item that does not fit (a mixin, so vanilla and other
 * mods' screens obey it too), ground pickups only happen when the item fits, and
 * {@link #tidy} moves items that something else put where they do not fit.
 * Creative and spectator players are never limited.
 */
public final class ItemSizes {
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    /** {@link Inventory} slot of the main grid's first cell. */
    public static final int MAIN_START = 9;
    public static final int MAIN_END = MAIN_START + COLUMNS * ROWS;
    public static final int HOTBAR_SIZE = 9;

    private ItemSizes() {
    }

    /** The rules the server (or a singleplayer world) enforces. */
    public static ItemSizeRules serverRules() {
        return DayZConfig.get().itemSizeRules();
    }

    /** Whether sizes limit this player under these rules. */
    public static boolean applies(Player player, ItemSizeRules rules) {
        return rules.enabled() && !player.isCreative() && !player.isSpectator();
    }

    public static ItemSizeRules.Size sizeOf(ItemStack stack, ItemSizeRules rules) {
        if (stack.isEmpty() || !rules.enabled()) return ItemSizeRules.ONE;
        Item item = stack.getItem();
        return rules.sizeOf(item, BuiltInRegistries.ITEM.getKey(item).toString(), tag -> hasTag(stack, tag));
    }

    private static boolean hasTag(ItemStack stack, String tag) {
        Identifier id = Identifier.tryParse(tag);
        return id != null && stack.is(TagKey.create(Registries.ITEM, id));
    }

    /** The main inventory of {@code inventory} as a grid. */
    public static GridLayout layout(Container inventory, ItemSizeRules rules) {
        int cells = COLUMNS * ROWS;
        int[] widths = new int[cells];
        int[] heights = new int[cells];
        for (int cell = 0; cell < cells; cell++) {
            ItemStack stack = inventory.getItem(MAIN_START + cell);
            if (!stack.isEmpty()) {
                ItemSizeRules.Size size = sizeOf(stack, rules);
                widths[cell] = size.width();
                heights[cell] = size.height();
            }
        }
        return GridLayout.of(COLUMNS, ROWS, widths, heights);
    }

    /**
     * Whether {@code stack} may be put into slot {@code inventorySlot} of a
     * player inventory, replacing what is there. Slots outside the main grid,
     * and everything while sizes are off, always may.
     */
    public static boolean mayPlace(Container inventory, int inventorySlot, ItemStack stack, ItemSizeRules rules) {
        if (!rules.enabled() || stack.isEmpty() || inventorySlot < MAIN_START || inventorySlot >= MAIN_END) {
            return true;
        }
        ItemSizeRules.Size size = sizeOf(stack, rules);
        return layout(inventory, rules).canPlace(inventorySlot - MAIN_START, size.width(), size.height());
    }

    // ------------------------------------------------------------------
    // Adding items
    // ------------------------------------------------------------------

    /**
     * Puts as much of {@code stack} into the player's inventory as fits under
     * the server's sizes, the way vanilla's {@code Inventory#add} does: onto
     * matching stacks first, then into an empty hotbar slot, then wherever the
     * item's footprint fits. {@code stack} is shrunk by what went in.
     */
    public static void add(Player player, ItemStack stack) {
        ItemSizeRules rules = serverRules();
        Inventory inventory = player.getInventory();
        if (!applies(player, rules)) {
            inventory.add(stack);
            return;
        }
        int limit = Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize());

        for (int slot = 0; slot < MAIN_END && !stack.isEmpty(); slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (!existing.isEmpty() && DayZInventoryScreenHandler.sameItem(existing, stack) && existing.getCount() < limit) {
                int moved = Math.min(limit - existing.getCount(), stack.getCount());
                existing.grow(moved);
                stack.shrink(moved);
                inventory.setChanged();
            }
        }
        for (int slot = 0; slot < HOTBAR_SIZE && !stack.isEmpty(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                inventory.setItem(slot, stack.split(limit));
            }
        }
        ItemSizeRules.Size size = sizeOf(stack, rules);
        while (!stack.isEmpty()) {
            int cell = layout(inventory, rules).findPlace(size.width(), size.height());
            if (cell == GridLayout.NONE) break;
            inventory.setItem(MAIN_START + cell, stack.split(limit));
        }
    }

    /** Whether at least one of {@code stack} would go in through {@link #add}. */
    public static boolean hasRoom(Player player, ItemStack stack) {
        ItemSizeRules rules = serverRules();
        Inventory inventory = player.getInventory();
        if (!applies(player, rules) || stack.isEmpty()) return true;
        int limit = Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize());
        for (int slot = 0; slot < MAIN_END; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (slot < HOTBAR_SIZE && existing.isEmpty()) return true;
            if (!existing.isEmpty() && DayZInventoryScreenHandler.sameItem(existing, stack) && existing.getCount() < limit) {
                return true;
            }
        }
        ItemSizeRules.Size size = sizeOf(stack, rules);
        return layout(inventory, rules).findPlace(size.width(), size.height()) != GridLayout.NONE;
    }

    // ------------------------------------------------------------------
    // Tidying
    // ------------------------------------------------------------------

    /**
     * Moves every overflow item - one that does not fit where it is, because a
     * command, another mod or an older world put it there - to the first place
     * its footprint fits, or else to an empty hotbar slot. An item with nowhere
     * to go stays where it is: sizes never drop or delete anything. Runs on the
     * server every few ticks.
     */
    public static void tidy(Player player) {
        ItemSizeRules rules = serverRules();
        if (!applies(player, rules)) return;
        Inventory inventory = player.getInventory();
        boolean[] stuck = new boolean[COLUMNS * ROWS];

        for (int attempt = 0; attempt < COLUMNS * ROWS; attempt++) {
            GridLayout layout = layout(inventory, rules);
            int from = GridLayout.NONE;
            for (int cell = 0; cell < layout.cells(); cell++) {
                if (layout.isOverflow(cell) && !stuck[cell]) {
                    from = cell;
                    break;
                }
            }
            if (from == GridLayout.NONE) return;

            ItemStack stack = inventory.getItem(MAIN_START + from);
            ItemSizeRules.Size size = sizeOf(stack, rules);
            int to = layout.without(from).findPlace(size.width(), size.height());
            int target = to != GridLayout.NONE && to != from ? MAIN_START + to : firstEmptyHotbarSlot(inventory);
            if (target < 0) {
                stuck[from] = true;
                continue;
            }
            inventory.setItem(MAIN_START + from, ItemStack.EMPTY);
            inventory.setItem(target, stack);
        }
    }

    private static int firstEmptyHotbarSlot(Inventory inventory) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (inventory.getItem(slot).isEmpty()) return slot;
        }
        return -1;
    }
}
