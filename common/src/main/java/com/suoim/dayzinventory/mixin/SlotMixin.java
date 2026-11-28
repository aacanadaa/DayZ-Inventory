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
package com.suoim.dayzinventory.mixin;

import com.suoim.dayzinventory.ItemSizes;
import com.suoim.dayzinventory.grid.ItemSizeRules;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item sizes for every slot over a player's main inventory, on the server.
 * <p>
 * Vanilla's inventory, chest and furnace screens - and other mods' screens -
 * all show the player's inventory through plain {@link Slot}s, so refusing a
 * misfit here keeps every one of them from putting an item where its footprint
 * does not fit. Shift-click works out of the box too: it only moves an item into
 * an empty slot that {@code mayPlace} accepts, so it finds a place that fits.
 * <p>
 * Only the server side is limited. The DayZ screen predicts the same answer on
 * the client from the rules the server sent it; other screens simply get the
 * slot corrected by the server, as for any refused click. Nothing is shadowed:
 * {@code container} and {@code getContainerSlot()} are public.
 */
@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void dayz$itemSizes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;
        if (!(self.container instanceof Inventory inventory) || inventory.player.level().isClientSide()) {
            return;
        }
        ItemSizeRules rules = ItemSizes.serverRules();
        if (ItemSizes.applies(inventory.player, rules)
                && !ItemSizes.mayPlace(inventory, self.getContainerSlot(), stack, rules)) {
            cir.setReturnValue(false);
        }
    }
}
