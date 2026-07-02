package com.suoim.dayzinventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DayZInventoryScreenHandler extends AbstractContainerMenu {
    private final Container containerInventory;
    private final BlockPos containerPos;
    private final Inventory playerInventory;

    // Client-side constructor
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory, 
             buf.readBoolean() ? new SimpleContainer(buf.readInt()) : null, 
             buf.readBoolean() ? buf.readBlockPos() : null);
    }

    // Main constructor (used by client-side builder and server-side opener)
    public DayZInventoryScreenHandler(int syncId, Inventory playerInventory, @Nullable Container containerInventory, @Nullable BlockPos containerPos) {
        super(DayZInventory.DAYZ_INVENTORY_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;
        this.containerInventory = containerInventory;
        this.containerPos = containerPos;

        int containerSize = containerInventory != null ? containerInventory.getContainerSize() : 0;

        // 1. Add Container Slots (first index)
        if (containerInventory != null) {
            if (!playerInventory.player.level().isClientSide) {
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
                return LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(playerInventory, 38, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(playerInventory, 37, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.LEGS;
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(playerInventory, 36, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.FEET;
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
        if (this.containerInventory != null && !player.level().isClientSide) {
            this.containerInventory.stopOpen(player);
        }
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
                EquipmentSlot equipmentSlot = LivingEntity.getEquipmentSlotForItem(itemStack);
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
