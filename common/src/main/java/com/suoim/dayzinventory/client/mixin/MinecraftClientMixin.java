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
package com.suoim.dayzinventory.client.mixin;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    /**
     * Redirects the vanilla inventory screen to the DayZ screen for survival
     * players by cancelling the screen open and asking the server to open its
     * own menu instead.
     */
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof InventoryScreen)) {
            return;
        }

        // Deliberate passthrough used by the Curios/Trinkets buttons.
        if (Platform.allowVanillaInventory) {
            return;
        }

        // Never block the vanilla UI if the loader has not installed its helper
        // yet - better to show the vanilla screen than to crash or show nothing.
        if (!Platform.isReady()) {
            return;
        }

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && !localPlayer.isCreative()) {
            ci.cancel();
            // Send custom open inventory packet to server using Platform helper
            Platform.HELPER.sendPacketToServer(DayZInventoryPackets.OPEN_INVENTORY_PACKET,
                new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer()));
        }
    }
}
