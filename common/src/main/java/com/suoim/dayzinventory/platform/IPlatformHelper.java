package com.suoim.dayzinventory.platform;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

public interface IPlatformHelper {
    boolean isModLoaded(String modId);
    
    void sendPacketToServer(ResourceLocation packetId, FriendlyByteBuf buf);
    
    void openPlayerInventory(ServerPlayer player);
    
    void openContainerInventory(ServerPlayer player, BlockPos pos);
    
    MenuType<DayZInventoryScreenHandler> getScreenHandlerType();
}
