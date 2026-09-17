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
 * The hook point has moved three times now, and the version that matters is the
 * one where the <em>assignment</em> moves, not the one where a method appears:
 * <ul>
 *   <li><b>1.20.1 - 1.20.5</b>: {@code isHovering(Slot, double, double)} exists,
 *       so this mixin is not the hook; {@code render} assigns {@code hoveredSlot}.</li>
 *   <li><b>1.21 - 1.21.5</b>: {@code isHovering} is gone and {@code render} walks
 *       the slot list, assigning {@code hoveredSlot} directly.</li>
 *   <li><b>1.21.6 - 26.0</b>: {@code renderContents} is split out of {@code render}
 *       and takes the assignment with it. Note this is <b>1.21.6</b>, not 1.21.11 -
 *       {@code renderContents} merely <em>appears</em> at 1.21.6 and
 *       {@code hoveredSlot} is written there from that version on, so hooking
 *       {@code render} on 1.21.6-1.21.10 finds no injection point at all. With
 *       {@code defaultRequire: 1} that is a hard crash at launch.</li>
 *   <li><b>26.1+</b>: the render-state rewrite renames it to
 *       {@code extractContents}. The assignment still happens there:
 *       {@code extractContents} opens with
 *       {@code this.hoveredSlot = this.getHoveredSlot(mouseX, mouseY)}.</li>
 * </ul>
 * <p>
 * {@code @Shadow} on this target does not resolve at runtime any more (both
 * {@code menu} and {@code hoveredSlot} failed with "was not located"). The mixin
 * therefore shadows nothing: it only calls into {@link DayZInventoryScreen},
 * which inherits those fields from {@link AbstractContainerScreen} and can read
 * and write them directly.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

    @Inject(
//? if >=26.1 {
        method = "extractContents",
//?} elif >=1.21.6 {
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
