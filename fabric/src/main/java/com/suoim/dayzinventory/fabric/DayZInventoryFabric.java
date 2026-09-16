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
package com.suoim.dayzinventory.fabric;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.fabric.platform.FabricPlatformHelper;
import com.suoim.dayzinventory.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
// The name here differs by version - ExtendedMenuType from 26.1, renamed from
// ExtendedScreenHandlerType - but it is always this import; the build rewrites it.
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if >=1.20.5 {
import com.suoim.dayzinventory.DayZInventoryOpenData;
import com.suoim.dayzinventory.DayZInventoryPayload;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
//?}

public class DayZInventoryFabric implements ModInitializer {
    public static final String MOD_ID = "dayz_inventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static MenuType<DayZInventoryScreenHandler> DAYZ_INVENTORY_SCREEN_HANDLER;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing DayZ Inventory Fabric Mod...");

        // Initialize platform helper first
        Platform.HELPER = new FabricPlatformHelper();

        // Register the menu type. From 1.20.5 the extended variant takes the codec
        // for its opening data as a second argument; before that the opening data
        // is written into a buffer by hand in the menu provider.
        DAYZ_INVENTORY_SCREEN_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                DayZInventoryPackets.id("dayz_inventory"),
//? if >=1.20.5 {
                new ExtendedMenuType<>(DayZInventoryScreenHandler::new, DayZInventoryOpenData.CODEC)
//?} else {
                new ExtendedScreenHandlerType<>(DayZInventoryScreenHandler::new)
//?}
        );

//? if >=1.20.5 {
        // 1.20.5 replaced per-channel receivers with a single typed payload.
        // Registering on the common entrypoint covers both sides.
        //
        // PayloadTypeRegistry#playC2S was renamed to serverboundPlay in 26.1;
        // serverboundPlay() returns the RegistryFriendlyByteBuf-typed registry,
        // which is what this payload's CODEC is declared against.
        PayloadTypeRegistry.serverboundPlay().register(DayZInventoryPayload.TYPE, DayZInventoryPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DayZInventoryPayload.TYPE, (payload, context) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
            DayZInventoryPackets.handlePacketOnServer(payload.packetId(), context.player(), buf);
        });
//?} else {
        // Before 1.20.5 each channel is a bare ResourceLocation with its own
        // receiver, and the handler is handed the buffer directly.
        ServerPlayNetworking.registerGlobalReceiver(DayZInventoryPackets.OPEN_CONTAINER_PACKET, (server, player, handler, buf, responseSender) -> {
            DayZInventoryPackets.handlePacketOnServer(DayZInventoryPackets.OPEN_CONTAINER_PACKET, player, buf);
        });

        ServerPlayNetworking.registerGlobalReceiver(DayZInventoryPackets.OPEN_INVENTORY_PACKET, (server, player, handler, buf, responseSender) -> {
            DayZInventoryPackets.handlePacketOnServer(DayZInventoryPackets.OPEN_INVENTORY_PACKET, player, buf);
        });

        ServerPlayNetworking.registerGlobalReceiver(DayZInventoryPackets.PICKUP_ITEM_PACKET, (server, player, handler, buf, responseSender) -> {
            DayZInventoryPackets.handlePacketOnServer(DayZInventoryPackets.PICKUP_ITEM_PACKET, player, buf);
        });

        ServerPlayNetworking.registerGlobalReceiver(DayZInventoryPackets.QUICK_PICKUP_ITEM_PACKET, (server, player, handler, buf, responseSender) -> {
            DayZInventoryPackets.handlePacketOnServer(DayZInventoryPackets.QUICK_PICKUP_ITEM_PACKET, player, buf);
        });
//?}
    }
}
