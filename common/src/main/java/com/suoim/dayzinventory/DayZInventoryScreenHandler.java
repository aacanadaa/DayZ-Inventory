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

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
import net.minecraft.world.item.crafting.CraftingInput;
//?}
import net.minecraft.world.item.crafting.CraftingRecipe;
//? if >=1.20.2 {
import net.minecraft.world.item.crafting.RecipeHolder;
//?}
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The DayZ menu: an optional container, the player's inventory, armour,
 * offhand and a 2x2 crafting grid.
 * <p>
 * Slot layout, with {@code c} the container size (0 when none is open):
 * <pre>
 *   [0, c)              container
 *   [c, c+27)           main inventory
 *   [c+27, c+36)        hotbar
 *   [c+36, c+40)        armour: head, chest, legs, feet
 *   c+40                offhand
 *   c+41                crafting result
 *   [c+42, c+46)        crafting grid
 * </pre>
 * The client screen positions these slots itself; their menu coordinates are
 * placeholders.
 */
public class DayZInventoryScreenHandler extends AbstractContainerMenu {
    private final Container containerInventory;
    private final BlockPos containerPos;
    private final Inventory playerInventory;
    private final int containerSize;

    private final net.minecraft.world.inventory.CraftingContainer craftSlots = new net.minecraft.world.inventory.TransientCraftingContainer(this, 2, 2);
    private final net.minecraft.world.inventory.ResultContainer resultSlots = new net.minecraft.world.inventory.ResultContainer();

    // NeoForge and Forge client-side constructor. IMenuTypeExtension and
    // IForgeMenuType still hand over a FriendlyByteBuf, whereas Fabric's
    // extended menu type passes typed opening data - so both shapes exist and
    // each loader binds to its own.
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory,
             buf.readBoolean() ? new SimpleContainer(buf.readInt()) : null,
             buf.readBoolean() ? buf.readBlockPos() : null);
    }

//? if >=1.20.5 {
    // Fabric client-side constructor. 1.20.5 passes the typed opening data produced
    // by ExtendedScreenHandlerFactory#getScreenOpeningData rather than a buffer.
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, DayZInventoryOpenData data) {
        this(syncId, playerInventory,
             data.hasContainer() ? new SimpleContainer(data.containerSize()) : null,
             data.hasPos() ? data.pos() : null);
    }
//?}

    // Main constructor (used by client-side builder and server-side opener)
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, @Nullable Container containerInventory, @Nullable BlockPos containerPos) {
        super(com.suoim.dayzinventory.platform.Platform.HELPER.getScreenHandlerType(), syncId);
        this.playerInventory = playerInventory;
        this.containerInventory = containerInventory;
        this.containerPos = containerPos;
        this.containerSize = containerInventory != null ? containerInventory.getContainerSize() : 0;

        // 1. Container slots
        if (containerInventory != null) {
            if (!playerInventory.player.level().isClientSide()) {
                containerInventory.startOpen(playerInventory.player);
            }
            for (int i = 0; i < this.containerSize; i++) {
                this.addSlot(new ContainerSlot(containerInventory, i));
            }
        }

        // 2. Main inventory (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 0, 0));
            }
        }

        // 3. Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 0, 0));
        }

        // 4. Armour: the Inventory indexes are 39 head, 38 chest, 37 legs, 36 feet.
        this.addSlot(new ArmorSlot(playerInventory, 39, EquipmentSlot.HEAD));
        this.addSlot(new ArmorSlot(playerInventory, 38, EquipmentSlot.CHEST));
        this.addSlot(new ArmorSlot(playerInventory, 37, EquipmentSlot.LEGS));
        this.addSlot(new ArmorSlot(playerInventory, 36, EquipmentSlot.FEET));

        // 5. Offhand
        this.addSlot(new Slot(playerInventory, 40, 0, 0));

        // 6. Crafting result, then the 2x2 grid
        this.addSlot(new net.minecraft.world.inventory.ResultSlot(playerInventory.player, this.craftSlots, this.resultSlots, 0, 0, 0));
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                this.addSlot(new Slot(this.craftSlots, c + r * 2, 0, 0));
            }
        }
    }

    // ------------------------------------------------------------------
    // Slot layout
    // ------------------------------------------------------------------

    public int getContainerSize() { return this.containerSize; }
    public int inventoryStart() { return this.containerSize; }
    public int hotbarStart() { return this.containerSize + 27; }
    public int hotbarEnd() { return this.containerSize + 36; }
    public int armorStart() { return this.containerSize + 36; }
    public int offhandIndex() { return this.containerSize + 40; }
    public int resultIndex() { return this.containerSize + 41; }
    public int craftStart() { return this.containerSize + 42; }
    public int craftEnd() { return this.containerSize + 46; }

    /** The menu slot an item equips into, or -1. */
    public int equipmentSlotIndex(Player player, ItemStack stack) {
        EquipmentSlot slot = player.getEquipmentSlotForItem(stack);
        if (slot == EquipmentSlot.HEAD) return armorStart();
        if (slot == EquipmentSlot.CHEST) return armorStart() + 1;
        if (slot == EquipmentSlot.LEGS) return armorStart() + 2;
        if (slot == EquipmentSlot.FEET) return armorStart() + 3;
        if (slot == EquipmentSlot.OFFHAND) return offhandIndex();
        return -1;
    }

    // ------------------------------------------------------------------
    // Crafting
    // ------------------------------------------------------------------

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (!this.playerInventory.player.level().isClientSide()) {
            this.updateCraftingResult();
        }
    }

    /**
     * Re-implements CraftingMenu.slotChangedCraftingGrid() inline.
     * The protected helper class trick (DayZInventoryCraftingHelper) causes
     * IllegalAccessError in production under Knot's classloader, so we replicate
     * the logic here using only public APIs and this-accessible protected methods.
     */
    private void updateCraftingResult() {
        if (this.playerInventory.player.level().isClientSide()) return;
        ServerPlayer serverPlayer = (ServerPlayer) this.playerInventory.player;

        ItemStack result = ItemStack.EMPTY;

//? if >=1.21 {
        // 1.21 changed this API twice over: the raw CraftingContainer is now a
        // CraftingInput value, and recipes come back wrapped in a RecipeHolder.
        //
        // The two remaining differences are handled as build-level replacements
        // rather than as inner conditionals: Stonecutter conditions cannot nest,
        // and this block already sits inside one.
        CraftingInput craftingInput = this.craftSlots.asCraftInput();

        Optional<RecipeHolder<CraftingRecipe>> optional = serverPlayer.level().getServer()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftingInput, serverPlayer.level());

        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> recipeHolder = optional.get();
            if (this.resultSlots.setRecipeUsed(serverPlayer, recipeHolder)) {
                result = recipeHolder.value().assemble(craftingInput);
            }
        }
//?} elif >=1.20.2 {
        // 1.20.2 wrapped recipes in a RecipeHolder, but kept the raw
        // CraftingContainer and the Level-taking setRecipeUsed overload.
        // CraftingInput only arrives in 1.21, and the HolderLookup-based
        // assemble/registryAccess pair only in 1.20.5 - both are absorbed by the
        // build-level replacements, which is why this branch also serves 1.20.5+.
        Optional<RecipeHolder<CraftingRecipe>> optional = serverPlayer.getServer()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, this.craftSlots, serverPlayer.level());

        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> recipeHolder = optional.get();
            if (this.resultSlots.setRecipeUsed(serverPlayer.level(), serverPlayer, recipeHolder)) {
                result = recipeHolder.value().assemble(this.craftSlots, serverPlayer.level().registryAccess());
            }
        }
//?} else {
        Optional<CraftingRecipe> optional = serverPlayer.getServer()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, this.craftSlots, serverPlayer.level());

        if (optional.isPresent()) {
            CraftingRecipe recipe = optional.get();
            if (this.resultSlots.setRecipeUsed(serverPlayer.level(), serverPlayer, recipe)) {
                result = recipe.assemble(this.craftSlots, serverPlayer.level().registryAccess());
            }
        }
//?}

        this.resultSlots.setItem(0, result);
        // The result slot sits after the container and the player's slots, not
        // at 0 as it does in vanilla's InventoryMenu. Sending it to slot 0 put a
        // ghost of the result into the first container or inventory slot until
        // the next sync.
        int resultIndex = resultIndex();
        this.setRemoteSlot(resultIndex, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
            this.containerId, this.incrementStateId(), resultIndex, result
        ));
    }

    // ------------------------------------------------------------------
    // Container
    // ------------------------------------------------------------------

    public @Nullable Container getContainerInventory() {
        return this.containerInventory;
    }

    public @Nullable BlockPos getContainerPos() {
        return this.containerPos;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.containerInventory != null) {
            return this.containerInventory.stillValid(player);
        }
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.containerInventory != null && !player.level().isClientSide()) {
            this.containerInventory.stopOpen(player);
        }
        this.clearContainer(player, this.craftSlots);
    }

    // ------------------------------------------------------------------
    // Shift-click
    // ------------------------------------------------------------------

    /**
     * Shift-click routing:
     * <ul>
     *   <li>container -> inventory (hotbar first, from the end, as vanilla does);</li>
     *   <li>inventory or hotbar -> the open container;</li>
     *   <li>with no container open: an equippable item goes to its empty armour
     *       or offhand slot, otherwise main inventory and hotbar swap - which is
     *       what was missing before, so shift-clicking simply did nothing;</li>
     *   <li>armour, offhand and crafting -> inventory.</li>
     * </ul>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        int inventoryStart = inventoryStart();
        int hotbarStart = hotbarStart();
        int hotbarEnd = hotbarEnd();

        if (index == resultIndex()) {
            if (!this.moveItemStackTo(moving, inventoryStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(moving, original);
        } else if (index < this.containerSize) {
            if (!this.moveItemStackTo(moving, inventoryStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < hotbarEnd) {
            if (this.containerSize > 0) {
                if (!this.moveItemStackTo(moving, 0, this.containerSize, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int equipIndex = equipmentSlotIndex(player, moving);
                boolean equipped = equipIndex >= 0
                    && !this.slots.get(equipIndex).hasItem()
                    && this.slots.get(equipIndex).mayPlace(moving)
                    && this.moveItemStackTo(moving, equipIndex, equipIndex + 1, false);
                if (!equipped) {
                    boolean moved = index < hotbarStart
                        ? this.moveItemStackTo(moving, hotbarStart, hotbarEnd, false)
                        : this.moveItemStackTo(moving, inventoryStart, hotbarStart, false);
                    if (!moved) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.moveItemStackTo(moving, inventoryStart, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (moving.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, moving);
        return original;
    }

    // ------------------------------------------------------------------
    // Sorting
    // ------------------------------------------------------------------

    /** Sorts the main inventory (not the hotbar), merging partial stacks. */
    public void sortPlayerInventory() {
        sortSlots(inventoryStart(), hotbarStart());
    }

    /** Sorts the open container, if it is plain storage. */
    public void sortContainer() {
        if (this.containerSize > 0 && DayZMenus.isSortable(this.containerInventory)) {
            sortSlots(0, this.containerSize);
        }
    }

    /**
     * Merges and sorts the items in {@code [from, to)} by item id, then name.
     * Nothing is changed unless every resulting stack fits the slot it lands in.
     */
    private void sortSlots(int from, int to) {
        List<ItemStack> merged = new ArrayList<>();
        for (int i = from; i < to; i++) {
            ItemStack stack = this.slots.get(i).getItem();
            if (stack.isEmpty()) continue;
            ItemStack remaining = stack.copy();
            for (ItemStack existing : merged) {
                if (remaining.isEmpty()) break;
                if (!sameItem(existing, remaining)) continue;
                int room = existing.getMaxStackSize() - existing.getCount();
                if (room <= 0) continue;
                int moved = Math.min(room, remaining.getCount());
                existing.grow(moved);
                remaining.shrink(moved);
            }
            if (!remaining.isEmpty()) {
                merged.add(remaining);
            }
        }

        merged.sort(Comparator
            .comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
            .thenComparing(stack -> stack.getHoverName().getString())
            .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed()));

        if (merged.size() > to - from) {
            return;
        }
        for (int i = 0; i < merged.size(); i++) {
            Slot target = this.slots.get(from + i);
            ItemStack stack = merged.get(i);
            if (!target.mayPlace(stack) || stack.getCount() > target.getMaxStackSize(stack)) {
                return;
            }
        }
        for (int i = from; i < to; i++) {
            int offset = i - from;
            this.slots.get(i).set(offset < merged.size() ? merged.get(offset) : ItemStack.EMPTY);
        }
        this.broadcastChanges();
    }

    /** Same item with the same data: the stacks may merge. */
    public static boolean sameItem(ItemStack a, ItemStack b) {
//? if >=1.20.5 {
        return ItemStack.isSameItemSameComponents(a, b);
//?} else {
        return ItemStack.isSameItemSameTags(a, b);
//?}
    }

    // ------------------------------------------------------------------
    // Slots
    // ------------------------------------------------------------------

    /**
     * A container slot that honours the container's own rules. A plain
     * {@link Slot} accepts anything, which let any item into a furnace's fuel or
     * a brewing stand's bottle slots, and a shulker box into a shulker box.
     */
    private static final class ContainerSlot extends Slot {
        ContainerSlot(Container container, int index) {
            super(container, index, 0, 0);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!this.container.canPlaceItem(this.getContainerSlot(), stack)) {
                return false;
            }
            return !(this.container instanceof ShulkerBoxBlockEntity) || stack.getItem().canFitInsideContainerItems();
        }
    }

    /**
     * An armour slot: only items for this body slot, one at a time, and an item
     * with Curse of Binding cannot be taken off outside creative mode - the rule
     * vanilla's own armour slots enforce.
     */
    private final class ArmorSlot extends Slot {
        private final EquipmentSlot equipmentSlot;

        ArmorSlot(Inventory inventory, int index, EquipmentSlot equipmentSlot) {
            super(inventory, index, 0, 0);
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return playerInventory.player.getEquipmentSlotForItem(stack) == this.equipmentSlot;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = this.getItem();
            if (!stack.isEmpty() && !player.isCreative() && hasBindingCurse(stack)) {
                return false;
            }
            return super.mayPickup(player);
        }
    }

    private static boolean hasBindingCurse(ItemStack stack) {
//? if >=1.21 {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.has(stack, net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
//?} else {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.hasBindingCurse(stack);
//?}
    }
}
