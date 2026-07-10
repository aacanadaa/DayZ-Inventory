package com.suoim.dayzinventory.fabric.client;

import com.suoim.dayzinventory.client.DayZInventoryScreen;
import com.suoim.dayzinventory.fabric.DayZInventoryFabric;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class DayZInventoryFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(DayZInventoryFabric.DAYZ_INVENTORY_SCREEN_HANDLER, DayZInventoryScreen::new);
    }
}
