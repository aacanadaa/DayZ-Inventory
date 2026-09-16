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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DayZInventoryPackets {
    // The channel id factory is version-dependent: the constructor was public
    // until 1.21 and fromNamespaceAndPath replaced it from 1.21 on. Keeping it in
    // one helper matters because DayZInventoryPayload's TYPE also needs an id, and
    // that file is wrapped whole in `//? if >=1.20.5` so it cannot hold
    // conditionals of its own.
    public static Identifier id(String path) {
//? if >=1.21 {
        return Identifier.fromNamespaceAndPath("dayz_inventory", path);
//?} else {
        return new ResourceLocation("dayz_inventory", path);
//?}
    }

    public static final Identifier OPEN_INVENTORY_PACKET = id("open_inventory");
    public static final Identifier PICKUP_ITEM_PACKET = id("pickup_item");
    public static final Identifier QUICK_PICKUP_ITEM_PACKET = id("quick_pickup_item");
    public static final Identifier OPEN_CONTAINER_PACKET = id("open_container");

    public static void handlePacketOnServer(Identifier packetId, ServerPlayer player, FriendlyByteBuf buf) {
        if (packetId.equals(OPEN_CONTAINER_PACKET)) {
            BlockPos pos = buf.readBlockPos();
            player.level().getServer().execute(() -> {
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 25.0) {
                    // Open container method
                    openContainerInventory(player, pos);
                }
            });
        } else if (packetId.equals(OPEN_INVENTORY_PACKET)) {
            player.level().getServer().execute(() -> {
                openPlayerInventory(player);
            });
        } else if (packetId.equals(PICKUP_ITEM_PACKET)) {
            int entityId = buf.readInt();
            int slotId = buf.readInt();
            int amount = buf.readInt();
            player.level().getServer().execute(() -> {
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
//? if >=26.3 {
                                            // 26.3 made the caller state whether the drop is
                                            // client-predicted. This runs in a serverbound packet
                                            // handler, so it is server-only by construction.
                                            player.drop(oldStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
//?} else {
                                            player.drop(oldStack, false);
//?}
                                        }
                                    // ItemStack#getTag was removed in 1.20.5 when item NBT became
                                    // data components; isSameItemSameComponents is the equivalent test.
//? if >=1.20.5 {
                                    } else if (ItemStack.isSameItemSameComponents(slotStack, stack)) {
//?} else {
                                    } else if (slotStack.is(stack.getItem()) && java.util.Objects.equals(slotStack.getTag(), stack.getTag())) {
//?}
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
            player.level().getServer().execute(() -> {
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
