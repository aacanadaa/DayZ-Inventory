package com.suoim.dayzinventory.client.mixin;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof InventoryScreen) {
            if (Platform.allowVanillaInventory) {
                return;
            }
            net.minecraft.client.player.LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null && !localPlayer.isCreative()) {
                ci.cancel();
                // Send custom open inventory packet to server using Platform helper
                Platform.HELPER.sendPacketToServer(DayZInventoryPackets.OPEN_INVENTORY_PACKET, 
                    new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer()));
            }
        }
    }
}
