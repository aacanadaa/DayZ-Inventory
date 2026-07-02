package com.suoim.dayzinventory.client;

import com.suoim.dayzinventory.DayZInventory;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class DayZInventoryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register the custom screen for our ScreenHandler
		MenuScreens.register(DayZInventory.DAYZ_INVENTORY_SCREEN_HANDLER, DayZInventoryScreen::new);
	}
}