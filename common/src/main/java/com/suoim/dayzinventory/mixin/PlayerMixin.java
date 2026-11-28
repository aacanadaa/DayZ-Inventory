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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Moves items that were put where they do not fit - by {@code /give}, a
 * crafting table's output, another mod, or by turning item sizes on in a full
 * inventory - to a place where they do. See {@link ItemSizes#tidy}.
 * <p>
 * {@code Player#tick} runs for server players too ({@code ServerPlayer#doTick}
 * calls it), four times a second is plenty, and the check costs nothing while
 * sizes are off.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void dayz$tidyItemSizes(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self instanceof ServerPlayer && self.tickCount % 5 == 0 && ItemSizes.serverRules().enabled()) {
            ItemSizes.tidy(self);
        }
    }
}
