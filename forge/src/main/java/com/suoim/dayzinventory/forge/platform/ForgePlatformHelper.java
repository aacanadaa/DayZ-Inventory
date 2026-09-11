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
package com.suoim.dayzinventory.forge.platform;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.forge.DayZInventoryForge;
import com.suoim.dayzinventory.forge.network.ModNetwork;
import com.suoim.dayzinventory.platform.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public void sendPacketToServer(ResourceLocation packetId, FriendlyByteBuf buf) {
        ModNetwork.INSTANCE.sendToServer(new ModNetwork.DayZInventoryPlatformPacket(packetId, buf));
    }

    @Override
    public void openPlayerInventory(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (syncId, playerInventory, p) -> new DayZInventoryScreenHandler(syncId, playerInventory, null, null),
            Component.translatable("container.inventory")
        ), buf -> {
            buf.writeBoolean(false); // No container
            buf.writeBoolean(false); // No container pos
        });
    }

    @Override
    public void openContainerInventory(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        Container container = null;
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
        } else if (level.getBlockEntity(pos) instanceof Container c) {
            container = c;
        }

        if (container != null) {
            final Container finalContainer = container;
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (syncId, playerInventory, p) -> new DayZInventoryScreenHandler(syncId, playerInventory, finalContainer, pos),
                state.getBlock().getName()
            ), buf -> {
                buf.writeBoolean(true); // Has container
                buf.writeInt(finalContainer.getContainerSize());
                buf.writeBoolean(true); // Has container pos
                buf.writeBlockPos(pos);
            });
        }
    }

    @Override
    public MenuType<DayZInventoryScreenHandler> getScreenHandlerType() {
        return DayZInventoryForge.DAYZ_INVENTORY_SCREEN_HANDLER.get();
    }
}
