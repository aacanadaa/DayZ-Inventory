package com.suoim.dayzinventory;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class DayZInventoryPackets {
    public static final ResourceLocation OPEN_INVENTORY_PACKET = DayZInventory.id("open_inventory");
    public static final ResourceLocation PICKUP_ITEM_PACKET = DayZInventory.id("pickup_item");
    public static final ResourceLocation QUICK_PICKUP_ITEM_PACKET = DayZInventory.id("quick_pickup_item");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_INVENTORY_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                DayZInventory.openPlayerInventory(player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PICKUP_ITEM_PACKET, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            int slotId = buf.readInt();
            int amount = buf.readInt();
            server.execute(() -> {
                Entity entity = player.level().getEntity(entityId);
                if (entity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
                    // Check distance
                    if (player.distanceToSqr(itemEntity) <= 16.0) { // 4 blocks sqr (safe limit)
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
                                    
                                    // Play pickup sound
                                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                                            (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
                                }
                            }
                        }
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(QUICK_PICKUP_ITEM_PACKET, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            server.execute(() -> {
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
        });
    }
}
