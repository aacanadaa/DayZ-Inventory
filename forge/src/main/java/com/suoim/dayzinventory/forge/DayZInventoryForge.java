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
package com.suoim.dayzinventory.forge;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.client.DayZInventoryScreen;
import com.suoim.dayzinventory.forge.network.ModNetwork;
import com.suoim.dayzinventory.forge.platform.ForgePlatformHelper;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
// EventBus 7, which Forge moved to in 1.21.6, split the package up: IEventBus
// became bus.EventBus and SubscribeEvent moved under listener.
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//?} else {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod("dayz_inventory")
public class DayZInventoryForge {
    public static final String MOD_ID = "dayz_inventory";

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final RegistryObject<MenuType<DayZInventoryScreenHandler>> DAYZ_INVENTORY_SCREEN_HANDLER = MENUS.register(
        "dayz_inventory",
        () -> IForgeMenuType.create(DayZInventoryScreenHandler::new)
    );

    // 1.21 takes the context as a constructor parameter. The 1.20.1 module read
    // it from the static FMLJavaModLoadingContext.get(), which no longer exists.
    public DayZInventoryForge(FMLJavaModLoadingContext context) {
        // Initialize platform helper first - nothing else may run before this,
        // and anything reachable from a static initializer would see it as null.
        Platform.HELPER = new ForgePlatformHelper();

        // `var` because the bus type itself is renamed between EventBus 6 and 7.
        var modEventBus = context.getModEventBus();

        // Register registries
        MENUS.register(modEventBus);

        // Register setup events
        ModNetwork.register();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // MenuScreens.register is private in vanilla 1.21. NeoForge added
                // a RegisterMenuScreensEvent for this; Forge did not, and instead
                // ships an access transformer that makes this method and its
                // ScreenConstructor parameter public.
                MenuScreens.register(
                    DAYZ_INVENTORY_SCREEN_HANDLER.get(),
                    DayZInventoryScreen::new
                );
            });
        }
    }
}
