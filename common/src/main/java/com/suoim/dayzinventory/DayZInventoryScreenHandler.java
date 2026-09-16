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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class DayZInventoryScreenHandler extends AbstractContainerMenu {
    private final Container containerInventory;
    private final BlockPos containerPos;
    private final Inventory playerInventory;

    // NeoForge client-side constructor. IMenuTypeExtension still hands over a
    // FriendlyByteBuf, whereas Fabric's ExtendedScreenHandlerType passes typed
    // opening data - so both shapes exist and each loader binds to its own.
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory,
             buf.readBoolean() ? new SimpleContainer(buf.readInt()) : null,
             buf.readBoolean() ? buf.readBlockPos() : null);
    }

    // Fabric client-side constructor. 1.21 passes the typed opening data produced
    // by ExtendedScreenHandlerFactory#getScreenOpeningData rather than a buffer.
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, DayZInventoryOpenData data) {
        this(syncId, playerInventory,
             data.hasContainer() ? new SimpleContainer(data.containerSize()) : null,
             data.hasPos() ? data.pos() : null);
    }

    // Main constructor (used by client-side builder and server-side opener)
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, @Nullable Container containerInventory, @Nullable BlockPos containerPos) {
        super(com.suoim.dayzinventory.platform.Platform.HELPER.getScreenHandlerType(), syncId);
        this.playerInventory = playerInventory;
        this.containerInventory = containerInventory;
        this.containerPos = containerPos;

        int containerSize = containerInventory != null ? containerInventory.getContainerSize() : 0;

        // 1. Add Container Slots (first index)
        if (containerInventory != null) {
            if (!playerInventory.player.level().isClientSide()) {
                containerInventory.startOpen(playerInventory.player);
            }
            for (int i = 0; i < containerSize; i++) {
                // Coordinates set to 0 initially, will be placed dynamically during client rendering
                this.addSlot(new Slot(containerInventory, i, 0, 0));
            }
        }

        // 2. Add Player Inventory Slots (27 slots, index containerSize to containerSize + 26)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                this.addSlot(new Slot(playerInventory, slotIndex, 0, 0));
            }
        }

        // 3. Add Player Hotbar Slots (9 slots, index containerSize + 27 to containerSize + 35)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 0, 0));
        }

        // 4. Add Player Armor Slots (4 slots: Helmet, Chestplate, Leggings, Boots, index containerSize + 36 to containerSize + 39)
        // Note: Helmet is slot 39, Chestplate 38, Leggings 37, Boots 36 in player inventory
        this.addSlot(new Slot(playerInventory, 39, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return playerInventory.player.getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(playerInventory, 38, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return playerInventory.player.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(playerInventory, 37, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return playerInventory.player.getEquipmentSlotForItem(stack) == EquipmentSlot.LEGS;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(playerInventory, 36, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return playerInventory.player.getEquipmentSlotForItem(stack) == EquipmentSlot.FEET;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // 5. Add Offhand Slot (index containerSize + 40)
        this.addSlot(new Slot(playerInventory, 40, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        // 6. Add Crafting Slots (index containerSize + 41 is Result, containerSize + 42 to + 45 are inputs)
        this.addSlot(new net.minecraft.world.inventory.ResultSlot(playerInventory.player, this.craftSlots, this.resultSlots, 0, 0, 0));
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                this.addSlot(new Slot(this.craftSlots, c + r * 2, 0, 0));
            }
        }
    }

    private final net.minecraft.world.inventory.CraftingContainer craftSlots = new net.minecraft.world.inventory.TransientCraftingContainer(this, 2, 2);
    private final net.minecraft.world.inventory.ResultContainer resultSlots = new net.minecraft.world.inventory.ResultContainer();

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

        // 1.21 changed this API twice over: the raw CraftingContainer is now a
        // CraftingInput value, and recipes come back wrapped in a RecipeHolder.
        CraftingInput craftingInput = this.craftSlots.asCraftInput();

        Optional<RecipeHolder<CraftingRecipe>> optional = serverPlayer.level().getServer()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftingInput, serverPlayer.level());

        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> recipeHolder = optional.get();
//? if >=1.21.11 {
            if (this.resultSlots.setRecipeUsed(serverPlayer, recipeHolder)) {
//?} else {
            if (this.resultSlots.setRecipeUsed(serverPlayer.level(), serverPlayer, recipeHolder)) {
//?}
//? if >=26.2 {
                // 26.2 dropped the RegistryAccess parameter from Recipe#assemble -
                // it now takes only the CraftingInput.
                result = recipeHolder.value().assemble(craftingInput);
//?} else {
                result = recipeHolder.value().assemble(craftingInput, serverPlayer.level().registryAccess());
//?}
            }
        }

        this.resultSlots.setItem(0, result);
        // setRemoteSlot is protected in AbstractContainerMenu but accessible here since we extend it
        this.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
            this.containerId, this.incrementStateId(), 0, result
        ));
    }

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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int containerSize = this.containerInventory != null ? this.containerInventory.getContainerSize() : 0;
            
            if (index < containerSize) {
                // From container to player inventory/hotbar
                if (!this.moveItemStackTo(itemStack2, containerSize, containerSize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= containerSize && index < containerSize + 36) {
                // From player inventory/hotbar
                // 1. Try to move to armor/offhand if applicable
                boolean movedToArmor = false;
                EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
                int targetIdx = -1;
                
                if (equipmentSlot == EquipmentSlot.HEAD) targetIdx = containerSize + 36;
                else if (equipmentSlot == EquipmentSlot.CHEST) targetIdx = containerSize + 37;
                else if (equipmentSlot == EquipmentSlot.LEGS) targetIdx = containerSize + 38;
                else if (equipmentSlot == EquipmentSlot.FEET) targetIdx = containerSize + 39;
                else if (equipmentSlot == EquipmentSlot.OFFHAND) targetIdx = containerSize + 40;
                
                if (targetIdx != -1) {
                    Slot targetSlot = this.slots.get(targetIdx);
                    if (!targetSlot.hasItem() && targetSlot.mayPlace(itemStack2)) {
                        if (!this.moveItemStackTo(itemStack2, targetIdx, targetIdx + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                        movedToArmor = true;
                    }
                }
                
                // 2. Try to move to container (if open)
                if (!movedToArmor && containerSize > 0) {
                    if (!this.moveItemStackTo(itemStack2, 0, containerSize, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (index >= containerSize + 41) {
                // From crafting slots to player inventory/hotbar
                if (!this.moveItemStackTo(itemStack2, containerSize, containerSize + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From armor/offhand to player inventory/hotbar
                if (!this.moveItemStackTo(itemStack2, containerSize, containerSize + 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }
}
