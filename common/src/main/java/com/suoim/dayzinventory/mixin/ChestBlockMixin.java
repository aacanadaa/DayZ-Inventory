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

import com.suoim.dayzinventory.DayZMenus;
import com.suoim.dayzinventory.config.DayZConfig;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
//? if <1.20.5 {
import net.minecraft.world.InteractionHand;
//?}
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    // The container-open hook sits on a different method either side of 1.20.5.
    // That release split Block#use into useItemOn (held item) and useWithoutItem,
    // and opening a chest with an empty hand goes through the latter - which is
    // also what vanilla ChestBlock overrides. Below 1.20.5 there is only `use`,
    // and it carries the InteractionHand as an extra parameter, so the target
    // name and the signature both change while the body stays the same.
    //? if >=1.20.5 {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
    //?} else {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
    //?}
        // Fall through to vanilla behaviour if the loader has not installed its
        // platform helper yet, or if the config hands containers back to the
        // vanilla screen.
        if (!Platform.isReady() || !DayZConfig.get().openContainersInDayZScreen) {
            return;
        }

        // A locked or blocked container simply does not open, exactly as in
        // vanilla - DayZMenus applies those checks itself, including the
        // "is locked" message.
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            DayZMenus.openContainer(serverPlayer, pos);
        }
//? if >=1.21.2 {
        // sidedSuccess(bool) was removed in 1.21.2; pick the constant for the side
        // we are on instead.
        cir.setReturnValue(level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
//?} else {
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
//?}
    }
}
