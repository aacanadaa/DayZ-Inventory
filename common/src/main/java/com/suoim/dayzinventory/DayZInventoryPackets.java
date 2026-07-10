package com.suoim.dayzinventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DayZInventoryPackets {
    public static final ResourceLocation OPEN_INVENTORY_PACKET = new ResourceLocation("dayz_inventory", "open_inventory");
    public static final ResourceLocation PICKUP_ITEM_PACKET = new ResourceLocation("dayz_inventory", "pickup_item");
    public static final ResourceLocation QUICK_PICKUP_ITEM_PACKET = new ResourceLocation("dayz_inventory", "quick_pickup_item");
    public static final ResourceLocation OPEN_CONTAINER_PACKET = new ResourceLocation("dayz_inventory", "open_container");

    public static void handlePacketOnServer(ResourceLocation packetId, ServerPlayer player, FriendlyByteBuf buf) {
        if (packetId.equals(OPEN_CONTAINER_PACKET)) {
            BlockPos pos = buf.readBlockPos();
            player.server.execute(() -> {
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 25.0) {
                    // Open container method
                    openContainerInventory(player, pos);
                }
            });
        } else if (packetId.equals(OPEN_INVENTORY_PACKET)) {
            player.server.execute(() -> {
                openPlayerInventory(player);
            });
        } else if (packetId.equals(PICKUP_ITEM_PACKET)) {
            int entityId = buf.readInt();
            int slotId = buf.readInt();
            int amount = buf.readInt();
            player.server.execute(() -> {
                Entity entity = player.level().getEntity(entityId);
                if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
                    if (player.distanceToSqr(itemEntity) <= 16.0) {
                        ItemStack stack = itemEntity.getItem();
                        if (!stack.isEmpty() && player.containerMenu instanceof DayZInventoryScreenHandler handlerScreen) {
                            if (slotId >= 0 && slotId < handlerScreen.slots.size()) {
                                Slot slot = handlerScreen.getSlot(slotId);
                                if (slot.mayPlace(stack)) {
                                    ItemStack slotStack = slot.getItem();
                                    int toAdd = Math.min(amount, stack.getCount());
                                    if (slotStack.isEmpty()) {
                                        ItemStack newStack = stack.copy();
                                        newStack.setCount(toAdd);
                                        slot.set(newStack);
                                        stack.shrink(toAdd);
                                    } else if (slot.getMaxStackSize() == 1) {
                                        ItemStack oldStack = slotStack.copy();
                                        ItemStack newStack = stack.copy();
                                        newStack.setCount(1);
                                        slot.set(newStack);
                                        stack.shrink(1);
                                        if (!player.getInventory().add(oldStack)) {
                                            player.drop(oldStack, false);
                                        }
                                    } else if (slotStack.is(stack.getItem()) && java.util.Objects.equals(slotStack.getTag(), stack.getTag())) {
                                        int max = Math.min(slot.getMaxStackSize(slotStack), slotStack.getMaxStackSize());
                                        int addable = max - slotStack.getCount();
                                        int added = Math.min(toAdd, addable);
                                        slotStack.grow(added);
                                        stack.shrink(added);
                                        slot.setChanged();
                                    }
                                    
                                    if (stack.isEmpty()) {
                                        itemEntity.discard();
                                    } else {
                                        itemEntity.setItem(stack);
                                    }
                                    
                                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                                            (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
                                }
                            }
                        }
                    }
                }
            });
        } else if (packetId.equals(QUICK_PICKUP_ITEM_PACKET)) {
            int entityId = buf.readInt();
            player.server.execute(() -> {
                Entity entity = player.level().getEntity(entityId);
                if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
                    if (player.distanceToSqr(itemEntity) <= 16.0) {
                        ItemStack stack = itemEntity.getItem();
                        if (!stack.isEmpty()) {
                            ItemStack remaining = stack.copy();
                            if (player.getInventory().add(remaining)) {
                                itemEntity.discard();
                            } else {
                                itemEntity.setItem(remaining);
                            }
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                                    (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
                        }
                    }
                }
            });
        }
    }

    private static void openPlayerInventory(ServerPlayer player) {
        // Forward call to the platform helper
        com.suoim.dayzinventory.platform.Platform.HELPER.openPlayerInventory(player);
    }

    private static void openContainerInventory(ServerPlayer player, BlockPos pos) {
        // Forward call to the platform helper
        com.suoim.dayzinventory.platform.Platform.HELPER.openContainerInventory(player, pos);
    }
}
