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
package com.suoim.dayzinventory.client.mixin.compat;

import com.suoim.dayzinventory.client.DayZInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import yalter.mousetweaks.api.IMTModGuiContainer3Ex;

import java.util.List;

/**
 * Mouse Tweaks support.
 * <p>
 * Mouse Tweaks finds the slot under the cursor through vanilla's private
 * slot lookup, using unscaled GUI coordinates. The DayZ screen lays itself out
 * under its own scale whenever the window is narrower than the panel (for
 * example at 1080p with the automatic GUI scale), so that lookup missed every
 * slot and Mouse Tweaks appeared not to work in the DayZ inventory at all.
 * <p>
 * Implementing Mouse Tweaks' API lets the screen answer those questions itself.
 * The interface is added by this mixin rather than declared on the screen so
 * that the screen class does not depend on Mouse Tweaks: this mixin is only
 * applied when Mouse Tweaks is installed (see {@code CompatMixinPlugin}).
 */
@Mixin(value = DayZInventoryScreen.class, remap = false)
public abstract class MouseTweaksCompatMixin implements IMTModGuiContainer3Ex {
    private DayZInventoryScreen dayz$self() {
        return (DayZInventoryScreen) (Object) this;
    }

    @Override
    public boolean MT_isMouseTweaksDisabled() {
        return false;
    }

    @Override
    public boolean MT_isWheelTweakDisabled() {
        // The wheel scrolls the Vicinity panel; moving items there instead would
        // make it impossible to scroll past an open container.
        return dayz$self().isCursorOverVicinity();
    }

    @Override
    public List<Slot> MT_getSlots() {
        return dayz$self().getMenu().slots;
    }

    @Override
    public Slot MT_getSlotUnderMouse(double mouseX, double mouseY) {
        return dayz$self().findSlotAtScreenPosition(mouseX, mouseY);
    }

    @Override
    public boolean MT_isCraftingOutput(Slot slot) {
        return slot instanceof ResultSlot;
    }

    @Override
    public boolean MT_isIgnored(Slot slot) {
        return false;
    }

    @Override
    public boolean MT_disableRMBDraggingFunctionality() {
        return dayz$self().cancelRightClickDrag();
    }

    @Override
    public void MT_clickSlot(Slot slot, int mouseButton, ContainerInput containerInput) {
        dayz$self().clickSlotFromCompat(slot, mouseButton, containerInput);
    }
}
