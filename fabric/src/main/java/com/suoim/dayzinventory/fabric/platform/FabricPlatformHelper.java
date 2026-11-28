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

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.fabric.DayZInventoryFabric;
import com.suoim.dayzinventory.platform.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
// Raw below 26.1, renamed to ExtendedMenuProvider from 26.1; the build rewrites it.
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
//? if >=1.20.5 {
import com.suoim.dayzinventory.DayZInventoryOpenData;
import com.suoim.dayzinventory.DayZInventoryPayload;
//?} else {
import com.suoim.dayzinventory.DayZInventoryPackets;
//?}

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void sendPacketToServer(Identifier packetId, FriendlyByteBuf buf) {
//? if >=1.20.5 {
        // 1.20.5+ sends typed payloads; there is no longer a (channel, buffer) send.
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        ClientPlayNetworking.send(new DayZInventoryPayload(packetId, bytes));
//?} else {
        ClientPlayNetworking.send(packetId, buf);
//?}
    }

    @Override
    public boolean isServerModPresent() {
        // Fabric negotiates channels with the server, and a vanilla server
        // registers none of ours, so this is false there.
//? if >=1.20.5 {
        return ClientPlayNetworking.canSend(DayZInventoryPayload.TYPE.id());
//?} else {
        return ClientPlayNetworking.canSend(DayZInventoryPackets.OPEN_INVENTORY_PACKET);
//?}
    }

    @Override
    public void openMenu(ServerPlayer player, @Nullable Container container, @Nullable BlockPos pos, Component title) {
        int containerSize = container != null ? container.getContainerSize() : 0;
//? if >=1.20.5 {
        player.openMenu(new ExtendedMenuProvider<DayZInventoryOpenData>() {
            @Override
            public DayZInventoryOpenData getScreenOpeningData(ServerPlayer player) {
                String itemSizes = com.suoim.dayzinventory.ItemSizes.serverRules().serialize();
                return container != null && pos != null
                    ? DayZInventoryOpenData.forContainer(containerSize, pos, itemSizes)
                    : DayZInventoryOpenData.none(itemSizes);
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return new DayZInventoryScreenHandler(syncId, playerInventory, container, pos);
            }
        });
//?} else {
        // Before 1.20.5 the factory writes its own opening data into the buffer.
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                DayZInventoryScreenHandler.writeOpenData(buf, container, pos);
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return new DayZInventoryScreenHandler(syncId, playerInventory, container, pos);
            }
        });
//?}
    }

    @Override
    public MenuType<DayZInventoryScreenHandler> getScreenHandlerType() {
        return DayZInventoryFabric.DAYZ_INVENTORY_SCREEN_HANDLER;
    }
}
