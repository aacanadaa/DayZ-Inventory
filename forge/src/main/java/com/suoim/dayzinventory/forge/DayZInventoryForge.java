package com.suoim.dayzinventory.forge;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.client.DayZInventoryScreen;
import com.suoim.dayzinventory.forge.network.ModNetwork;
import com.suoim.dayzinventory.forge.platform.ForgePlatformHelper;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

    public DayZInventoryForge() {
        // Initialize platform helper first
        Platform.HELPER = new ForgePlatformHelper();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register registries
        MENUS.register(modEventBus);

        // Register setup events
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.gui.screens.MenuScreens.register(
                    DAYZ_INVENTORY_SCREEN_HANDLER.get(),
                    DayZInventoryScreen::new
                );
            });
        }
    }
}
