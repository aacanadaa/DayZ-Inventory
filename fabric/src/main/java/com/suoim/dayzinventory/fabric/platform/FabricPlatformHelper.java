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
package com.suoim.dayzinventory.fabric.platform;

import com.suoim.dayzinventory.DayZInventoryOpenData;
import com.suoim.dayzinventory.DayZInventoryPayload;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.fabric.DayZInventoryFabric;
import com.suoim.dayzinventory.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public void sendPacketToServer(ResourceLocation packetId, FriendlyByteBuf buf) {
        // 1.20.5+ sends typed payloads; there is no longer a (channel, buffer) send.
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        ClientPlayNetworking.send(new DayZInventoryPayload(packetId, bytes));
    }

    @Override
    public void openPlayerInventory(ServerPlayer player) {
        player.openMenu(new ExtendedScreenHandlerFactory<DayZInventoryOpenData>() {
            @Override
            public DayZInventoryOpenData getScreenOpeningData(ServerPlayer player) {
                return DayZInventoryOpenData.none();
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("container.inventory");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return new DayZInventoryScreenHandler(syncId, playerInventory, null, null);
            }
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
            player.openMenu(new ExtendedScreenHandlerFactory<DayZInventoryOpenData>() {
                @Override
                public DayZInventoryOpenData getScreenOpeningData(ServerPlayer player) {
                    return DayZInventoryOpenData.forContainer(finalContainer.getContainerSize(), pos);
                }

                @Override
                public Component getDisplayName() {
                    return state.getBlock().getName();
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                    return new DayZInventoryScreenHandler(syncId, playerInventory, finalContainer, pos);
                }
            });
        }
    }

    @Override
    public MenuType<DayZInventoryScreenHandler> getScreenHandlerType() {
        return DayZInventoryFabric.DAYZ_INVENTORY_SCREEN_HANDLER;
    }
}
