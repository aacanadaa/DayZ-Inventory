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
package com.suoim.dayzinventory.neoforge;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.client.DayZInventoryScreen;
import com.suoim.dayzinventory.neoforge.platform.NeoForgePlatformHelper;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("dayz_inventory")
public class DayZInventoryNeoForge {
    public static final String MOD_ID = "dayz_inventory";

    private static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DayZInventoryScreenHandler>> DAYZ_INVENTORY_SCREEN_HANDLER =
        MENUS.register("dayz_inventory", () -> IMenuTypeExtension.create(DayZInventoryScreenHandler::new));

    public DayZInventoryNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Initialize platform helper first
        Platform.HELPER = new NeoForgePlatformHelper();

        MENUS.register(modEventBus);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        // MenuScreens.register is private in 1.21; NeoForge exposes a dedicated
        // event for screen registration instead.
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(DAYZ_INVENTORY_SCREEN_HANDLER.get(), DayZInventoryScreen::new);
        }
    }
}
