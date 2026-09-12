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

import com.suoim.dayzinventory.client.DayZInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the virtual Hands panel count as hovering the Hands slot.
 * <p>
 * This mixin deliberately shadows nothing. It used to {@code @Shadow} {@code menu},
 * {@code leftPos}, {@code topPos} and {@code imageHeight}, but none of those
 * shadows carry a refmap entry - so on the remapped runtimes (intermediary on
 * Fabric, SRG on Forge) Mixin went looking for fields literally named
 * {@code menu} and friends, failed with
 * {@code @Shadow field menu was not located in the target class}, and took the
 * game down on launch.
 * <p>
 * It only ever looked fine in development, where the game runs on official names
 * and the shadows resolve by name. The packaged jars crashed the moment the player
 * opened a container.
 * <p>
 * The check now lives in {@link DayZInventoryScreen#isVirtualHandsSlotHovered}.
 * That class inherits all four fields from {@link AbstractContainerScreen}, so it
 * can read them without shadowing anything.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void onIsHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DayZInventoryScreen dayZScreen
                && dayZScreen.isVirtualHandsSlotHovered(slot, mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }
}
