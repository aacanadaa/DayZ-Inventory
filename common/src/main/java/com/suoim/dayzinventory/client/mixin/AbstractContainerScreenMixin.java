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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the virtual Hands panel count as hovering the Hands slot.
 * <p>
 * The hook point has moved three times now. 1.21 removed
 * isHovering(Slot, double, double) and assigned hoveredSlot from render; 1.21.11
 * split the render pipeline so that assignment moved to renderContents; 26.2
 * renamed that method to extractContents as part of the render-state rewrite,
 * which is the current target. The assignment itself still happens there:
 * extractContents opens with {@code this.hoveredSlot = this.getHoveredSlot(mouseX, mouseY)}.
 * <p>
 * 1. {@code isHovering(Slot, double, double)} is gone - {@code render} now walks
 * the slot list and assigns {@code hoveredSlot} directly, so that assignment is
 * the hook point.
 * <p>
 * 2. {@code @Shadow} on this target does not resolve at runtime any more
 * (both {@code menu} and {@code hoveredSlot} failed with "was not located").
 * The mixin therefore shadows nothing: it only calls into
 * {@link DayZInventoryScreen}, which inherits those fields from
 * {@link AbstractContainerScreen} and can read and write them directly.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Inject(
//? if >=26.1 {
        method = "extractContents",
//?} elif >=1.21.11 {
        method = "renderContents",
//?} else {
        method = "render",
//?}
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hoveredSlot:Lnet/minecraft/world/inventory/Slot;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void dayz$applyVirtualHandsHover(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if ((Object) this instanceof DayZInventoryScreen dayZScreen) {
            dayZScreen.applyVirtualHandsHover(mouseX, mouseY);
        }
    }
}
