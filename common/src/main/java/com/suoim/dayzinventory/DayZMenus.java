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

import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side entry points for opening the DayZ menu, and the rules for which
 * containers it may open.
 * <p>
 * This used to be duplicated in every loader's platform helper. It is loader
 * independent, so it lives here, and the helpers only do the loader-specific
 * part: sending the menu with its opening data.
 */
public final class DayZMenus {
    private DayZMenus() {
    }

    /** Opens the player's own inventory in the DayZ screen. */
    public static void openPlayerInventory(ServerPlayer player) {
        if (player.isSpectator() || !player.isAlive()) {
            return;
        }
        Platform.HELPER.openMenu(player, null, null, Component.translatable("container.inventory"));
    }

    /**
     * Opens the container at {@code pos} in the DayZ screen.
     *
     * @return false when there is nothing there that may be opened - no
     *         container, a locked one, or a chest whose lid is blocked
     */
    public static boolean openContainer(ServerPlayer player, BlockPos pos) {
        Container container = resolveContainer(player, pos);
        if (container == null) {
            return false;
        }
        BlockState state = player.level().getBlockState(pos);
        Platform.HELPER.openMenu(player, container, pos.immutable(), state.getBlock().getName());
        return true;
    }

    /**
     * The container a player may open at {@code pos}, or null.
     * <p>
     * Applies the checks vanilla applies when the block is right-clicked, which
     * the Vicinity panel would otherwise skip: container locks (and the
     * "is locked" message that goes with them), a chest blocked by a solid block
     * or a cat, and loot tables, which are rolled for the opening player so luck
     * and the "opened a loot chest" advancement still apply.
     */
    public static @Nullable Container resolveContainer(ServerPlayer player, BlockPos pos) {
        if (player.isSpectator() || !player.isAlive()) {
            return null;
        }
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof BaseContainerBlockEntity base && !base.canOpen(player)) {
            return null;
        }

        Container container = null;
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            // `false`: honour a blocked lid, as vanilla does. The combined
            // container of a double chest comes back from either half.
            container = ChestBlock.getContainer(chestBlock, state, level, pos, false);
            if (container != null && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                unpackLoot(level.getBlockEntity(pos.relative(ChestBlock.getConnectedDirection(state))), player);
            }
        } else if (blockEntity instanceof Container direct) {
            container = direct;
        }

        if (container != null) {
            unpackLoot(blockEntity, player);
        }
        return container;
    }

    /**
     * Whether a container is plain storage that may be sorted. Machines (furnaces,
     * brewing stands, modded blocks) give their slots meaning, so they are never
     * rearranged.
     */
    public static boolean isSortable(@Nullable Object container) {
        return container instanceof CompoundContainer
            || container instanceof ChestBlockEntity
            || container instanceof BarrelBlockEntity
            || container instanceof ShulkerBoxBlockEntity
            || container instanceof DispenserBlockEntity
            || container instanceof HopperBlockEntity;
    }

    private static void unpackLoot(@Nullable BlockEntity blockEntity, ServerPlayer player) {
        if (blockEntity instanceof RandomizableContainerBlockEntity randomizable) {
            randomizable.unpackLootTable(player);
        }
    }
}
