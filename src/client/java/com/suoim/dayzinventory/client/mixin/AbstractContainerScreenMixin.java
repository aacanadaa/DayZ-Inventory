package com.suoim.dayzinventory.client.mixin;

import com.suoim.dayzinventory.client.DayZInventoryScreen;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Shadow protected T menu;
    @Shadow protected int leftPos;
    @Shadow protected int imageHeight;

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void onIsHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DayZInventoryScreen dayZScreen) {
            if (this.menu instanceof DayZInventoryScreenHandler handler) {
                int containerSize = handler.getContainerInventory() != null ? handler.getContainerInventory().getContainerSize() : 0;
                if (slot.index == containerSize + 27) {
                    // Check if hovering over physical slot in right column
                    boolean hoveringPhysical = mouseX >= slot.x && mouseX < (slot.x + 18) 
                        && mouseY >= slot.y && mouseY < (slot.y + 18);
                    
                    // Check if hovering over virtual hands slot in middle column
                    int middleColumnX = dayZScreen.getColumnX(1);
                    int handsSlotX = middleColumnX + 72 - this.leftPos;
                    int handsSlotY = this.imageHeight - 44;
                    boolean hoveringVirtual = mouseX >= (handsSlotX - 4) && mouseX < (handsSlotX + 22) 
                        && mouseY >= (handsSlotY - 4) && mouseY < (handsSlotY + 22);
                    
                    cir.setReturnValue(hoveringPhysical || hoveringVirtual);
                }
            }
        }
    }
}
