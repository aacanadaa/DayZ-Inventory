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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        // Called from the render loop, so this must stay cheap and must never
        // throw for a mod id that is not installed.
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void sendPacketToServer(Identifier packetId, FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        ModNetwork.sendToServer(packetId, bytes);
    }

    @Override
    public boolean isServerModPresent() {
        // Forge refuses to join a server whose channel version does not match,
        // so a connected server always has this mod.
        return true;
    }

    @Override
    public void openMenu(ServerPlayer player, @Nullable Container container, @Nullable BlockPos pos, Component title) {
        SimpleMenuProvider provider = new SimpleMenuProvider(
            (syncId, playerInventory, p) -> new DayZInventoryScreenHandler(syncId, playerInventory, container, pos),
            title
        );
        // The written buffer is handed to the menu type's IContainerFactory on
        // the client. Forge 1.20.1 sends it through NetworkHooks; from 1.20.6
        // IForgeServerPlayer adds the same overload to openMenu directly.
//? if >=1.20.6 {
        player.openMenu(provider, buf -> {
//?} else {
        net.minecraftforge.network.NetworkHooks.openScreen(player, provider, buf -> {
//?}
            DayZInventoryScreenHandler.writeOpenData(buf, container, pos);
        });
    }

    @Override
    public MenuType<DayZInventoryScreenHandler> getScreenHandlerType() {
        return DayZInventoryForge.DAYZ_INVENTORY_SCREEN_HANDLER.get();
    }
}
