package com.suoim.dayzinventory.fabric;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.fabric.platform.FabricPlatformHelper;
import com.suoim.dayzinventory.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DayZInventoryFabric implements ModInitializer {
    public static final String MOD_ID = "dayz_inventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static MenuType<DayZInventoryScreenHandler> DAYZ_INVENTORY_SCREEN_HANDLER;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing DayZ Inventory Fabric Mod...");

        // Initialize platform helper first
        Platform.HELPER = new FabricPlatformHelper();

        // Register screen handler type using Fabric ExtendedScreenHandlerType
        DAYZ_INVENTORY_SCREEN_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                new ResourceLocation(MOD_ID, "dayz_inventory"),
                new ExtendedScreenHandlerType<>(DayZInventoryScreenHandler::new)
        );

        // Register Server-Side Packet Receivers
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
    }
}
