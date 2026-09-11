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
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Shadow protected T menu;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageHeight;

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void onIsHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DayZInventoryScreen dayZScreen) {
            if (this.menu instanceof DayZInventoryScreenHandler handler) {
                int containerSize = handler.getContainerInventory() != null ? handler.getContainerInventory().getContainerSize() : 0;
                
                int selectedSlot = 0;
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    selectedSlot = mc.player.getInventory().selected;
                }
                int handsSlotIdx = containerSize + 27 + selectedSlot;

                if (slot.index == handsSlotIdx) {
                    // Check if hovering over physical slot in right column (absolute coordinates)
                    boolean hoveringPhysical = mouseX >= (this.leftPos + slot.x) && mouseX < (this.leftPos + slot.x + 18) 
                        && mouseY >= (this.topPos + slot.y) && mouseY < (this.topPos + slot.y + 18);
                    
                    // Check if hovering over virtual hands slot body in middle column (absolute coordinates)
                    int middleColumnX = dayZScreen.getColumnX(1);
                    int handsPanelY = this.topPos + this.imageHeight - 75;
                    
                    ItemStack handsStack = slot.getItem();
                    int bodyY = handsPanelY + (handsStack.isEmpty() ? 15 : 27);
                    int bodyHeight = handsStack.isEmpty() ? 55 : 43;
                    
                    boolean hoveringVirtual = mouseX >= (middleColumnX - 4) && mouseX < (middleColumnX + 166) 
                        && mouseY >= bodyY && mouseY < (bodyY + bodyHeight);
                    
                    cir.setReturnValue(hoveringPhysical || hoveringVirtual);
                }
            }
        }
    }
}
