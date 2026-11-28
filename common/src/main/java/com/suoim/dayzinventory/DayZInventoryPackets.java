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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The client-to-server messages and their server-side handling.
 * <p>
 * Every message travels in the same envelope - a sub-packet id plus raw bytes -
 * and each loader only supplies the transport. Payload fields are read on the
 * calling thread (which may be the network thread) and the work itself always
 * runs on the server thread.
 */
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
    public static final Identifier SORT_PACKET = id("sort");

    /** {@link #SORT_PACKET} targets. */
    public static final int SORT_INVENTORY = 0;
    public static final int SORT_CONTAINER = 1;

    public static void handlePacketOnServer(Identifier packetId, ServerPlayer player, FriendlyByteBuf buf) {
        if (packetId.equals(OPEN_CONTAINER_PACKET)) {
            BlockPos pos = buf.readBlockPos();
            onServerThread(player, () -> openContainerFromVicinity(player, pos));
        } else if (packetId.equals(OPEN_INVENTORY_PACKET)) {
            onServerThread(player, () -> DayZMenus.openPlayerInventory(player));
        } else if (packetId.equals(PICKUP_ITEM_PACKET)) {
            int entityId = buf.readInt();
            int slotId = buf.readInt();
            int amount = buf.readInt();
            onServerThread(player, () -> pickUpIntoSlot(player, entityId, slotId, amount));
        } else if (packetId.equals(QUICK_PICKUP_ITEM_PACKET)) {
            int entityId = buf.readInt();
            onServerThread(player, () -> quickPickUp(player, entityId));
        } else if (packetId.equals(SORT_PACKET)) {
            int target = buf.readByte();
            onServerThread(player, () -> sort(player, target));
        }
    }

    private static void onServerThread(ServerPlayer player, Runnable task) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            server.execute(task);
        }
    }

    // ------------------------------------------------------------------
    // Handlers
    // ------------------------------------------------------------------

    private static void openContainerFromVicinity(ServerPlayer player, BlockPos pos) {
        double reach = DayZConfig.get().vicinityRadius + 2.5;
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= reach * reach) {
            DayZMenus.openContainer(player, pos);
        }
    }

    /** Moves (part of) a ground item into a specific slot of the open DayZ menu. */
    private static void pickUpIntoSlot(ServerPlayer player, int entityId, int slotId, int amount) {
        ItemEntity itemEntity = reachableItem(player, entityId);
        if (itemEntity == null || amount <= 0) return;
        if (!(player.containerMenu instanceof DayZInventoryScreenHandler menu)) return;
        if (slotId < 0 || slotId >= menu.slots.size()) return;

        ItemStack stack = itemEntity.getItem();
        Slot slot = menu.getSlot(slotId);
        if (!slot.mayPlace(stack)) return;

        ItemStack inSlot = slot.getItem();
        int wanted = Math.min(amount, stack.getCount());
        int moved = 0;

        if (inSlot.isEmpty()) {
            // Capped by the slot: an armour slot holds one item, never a stack of
            // carved pumpkins.
            moved = Math.min(wanted, slot.getMaxStackSize(stack));
            ItemStack placed = stack.copy();
            placed.setCount(moved);
            slot.set(placed);
        } else if (DayZInventoryScreenHandler.sameItem(inSlot, stack)) {
            int max = Math.min(slot.getMaxStackSize(inSlot), inSlot.getMaxStackSize());
            moved = Math.max(0, Math.min(wanted, max - inSlot.getCount()));
            if (moved > 0) {
                inSlot.grow(moved);
                slot.setChanged();
            }
        } else if (slot.getMaxStackSize(stack) == 1 && slot.mayPickup(player)) {
            // A single-item slot (armour) holding something else: swap, and put
            // the old item in the inventory - or at the player's feet.
            ItemStack previous = inSlot.copy();
            ItemStack placed = stack.copy();
            placed.setCount(1);
            slot.set(placed);
            moved = 1;
            ItemSizes.add(player, previous);
            dropAtFeet(player, previous);
        }

        if (moved > 0) {
            finishPickUp(player, itemEntity, stack, moved);
        }
    }

    /**
     * Shift-click on a ground item: into the inventory, wherever it fits - with
     * item sizes on, only where its footprint fits; the rest stays on the ground.
     */
    private static void quickPickUp(ServerPlayer player, int entityId) {
        ItemEntity itemEntity = reachableItem(player, entityId);
        if (itemEntity == null) return;

        ItemStack stack = itemEntity.getItem();
        ItemStack remaining = stack.copy();
        ItemSizes.add(player, remaining);
        int moved = stack.getCount() - remaining.getCount();
        // Inventory#add reports success when *any* of the stack fitted, so the
        // entity may only be removed once nothing is left - discarding it on a
        // partial fit deleted the rest of the stack.
        if (moved > 0) {
            finishPickUp(player, itemEntity, stack, moved);
        }
    }

    private static void sort(ServerPlayer player, int target) {
        if (player.isSpectator() || !(player.containerMenu instanceof DayZInventoryScreenHandler menu)) return;
        if (target == SORT_INVENTORY) {
            menu.sortPlayerInventory();
        } else if (target == SORT_CONTAINER) {
            menu.sortContainer();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** The item entity, if it exists, is alive and is within the Vicinity reach. */
    private static ItemEntity reachableItem(ServerPlayer player, int entityId) {
        if (player.isSpectator() || !player.isAlive()) return null;
        Entity entity = player.level().getEntity(entityId);
        if (!(entity instanceof ItemEntity itemEntity) || !itemEntity.isAlive() || itemEntity.getItem().isEmpty()) {
            return null;
        }
        double reach = DayZConfig.get().vicinityRadius + 2.0;
        return player.distanceToSqr(itemEntity) <= reach * reach ? itemEntity : null;
    }

    /**
     * Removes {@code moved} items from the ground stack and does what vanilla
     * does on a pickup: the stat, the pickup animation others see, and the sound.
     */
    private static void finishPickUp(ServerPlayer player, ItemEntity itemEntity, ItemStack groundStack, int moved) {
        Item item = groundStack.getItem();
        player.take(itemEntity, moved);
        player.awardStat(Stats.ITEM_PICKED_UP.get(item), moved);

        ItemStack left = groundStack.copy();
        left.shrink(moved);
        if (left.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(left);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
    }

    private static void dropAtFeet(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
//? if >=26.3 {
        // 26.3 made the caller state whether the drop is client-predicted. This
        // runs in a serverbound packet handler, so it is server-only by
        // construction.
        player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
//?} else {
        player.drop(stack, false);
//?}
    }
}
