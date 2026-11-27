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
package com.suoim.dayzinventory.neoforge.platform;

import com.suoim.dayzinventory.DayZInventoryPayload;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.neoforge.DayZInventoryNeoForge;
import com.suoim.dayzinventory.platform.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void sendPacketToServer(Identifier packetId, FriendlyByteBuf buf) {
        // NeoForge uses the vanilla typed-payload system, same shape as Fabric's -
        // wrap the raw bytes and hand them to the distributor.
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        ClientPacketDistributor.sendToServer(new DayZInventoryPayload(packetId, bytes));
    }

    @Override
    public boolean isServerModPresent() {
        // The payload is registered as required, so NeoForge refuses to join a
        // server that does not have this mod: when connected, it is there.
        return true;
    }

    @Override
    public void openMenu(ServerPlayer player, @Nullable Container container, @Nullable BlockPos pos, Component title) {
        boolean hasContainer = container != null && pos != null;
        int containerSize = hasContainer ? container.getContainerSize() : 0;
        player.openMenu(new SimpleMenuProvider(
            (syncId, playerInventory, p) -> new DayZInventoryScreenHandler(syncId, playerInventory, container, pos),
            title
        ), buf -> {
            buf.writeBoolean(hasContainer);
            if (hasContainer) buf.writeInt(containerSize);
            buf.writeBoolean(hasContainer);
            if (hasContainer) buf.writeBlockPos(pos);
        });
    }

    @Override
    public MenuType<DayZInventoryScreenHandler> getScreenHandlerType() {
        return DayZInventoryNeoForge.DAYZ_INVENTORY_SCREEN_HANDLER.get();
    }
}
