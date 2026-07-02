package com.suoim.dayzinventory.client.mixin;

import com.suoim.dayzinventory.client.DayZInventoryScreen;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Shadow protected T menu;
    @Shadow protected Font font;

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void onRenderSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if ((Object) this instanceof DayZInventoryScreen) {
            if (this.menu instanceof DayZInventoryScreenHandler handler) {
                int containerSize = handler.getContainerInventory() != null ? handler.getContainerInventory().getContainerSize() : 0;
                if (slot.index == containerSize + 27) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        guiGraphics.pose().pushPose();
                        // Centering a 1.5x scaled item (24px) in a 26x26 slot.
                        // The slot's top-left corner is at (slot.x - 4, slot.y - 4).
                        // So the inner 24x24 area starts at (slot.x - 3, slot.y - 3).
                        guiGraphics.pose().translate(slot.x - 3, slot.y - 3, 100);
                        guiGraphics.pose().scale(1.5F, 1.5F, 1.0F);
                        
                        guiGraphics.renderFakeItem(stack, 0, 0);
                        guiGraphics.renderItemDecorations(this.font, stack, 0, 0);
                        guiGraphics.pose().popPose();
                    }
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void onIsHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DayZInventoryScreen) {
            if (this.menu instanceof DayZInventoryScreenHandler handler) {
                int containerSize = handler.getContainerInventory() != null ? handler.getContainerInventory().getContainerSize() : 0;
                if (slot.index == containerSize + 27) {
                    // Expanded large hover check (26x26)
                    boolean hovering = mouseX >= (slot.x - 4) && mouseX < (slot.x + 22) 
                        && mouseY >= (slot.y - 4) && mouseY < (slot.y + 22);
                    cir.setReturnValue(hovering);
                }
            }
        }
    }
}
