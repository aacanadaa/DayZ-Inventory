package com.suoim.dayzinventory.client.mixin;

import com.suoim.dayzinventory.DayZInventoryPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Shadow public net.minecraft.client.player.LocalPlayer player;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof InventoryScreen) {
            if (this.player != null && !this.player.isCreative()) {
                ci.cancel();
                // Send custom open inventory packet to server
                ClientPlayNetworking.send(DayZInventoryPackets.OPEN_INVENTORY_PACKET, PacketByteBufs.create());
            }
        }
    }
}
