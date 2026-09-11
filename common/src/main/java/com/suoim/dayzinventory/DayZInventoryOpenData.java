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
package com.suoim.dayzinventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data handed to the client when the DayZ screen is opened.
 * <p>
 * 1.20.5 replaced the "write into a FriendlyByteBuf by hand" contract of
 * {@code ExtendedScreenHandlerFactory} with a typed value plus a
 * {@link StreamCodec}. All fields are always encoded - absent values are
 * represented by {@link #none()} - which keeps the codec a plain composite
 * instead of a hand-rolled conditional reader.
 */
public record DayZInventoryOpenData(boolean hasContainer, int containerSize, boolean hasPos, BlockPos pos) {

    public static final StreamCodec<RegistryFriendlyByteBuf, DayZInventoryOpenData> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, DayZInventoryOpenData::hasContainer,
            ByteBufCodecs.VAR_INT, DayZInventoryOpenData::containerSize,
            ByteBufCodecs.BOOL, DayZInventoryOpenData::hasPos,
            BlockPos.STREAM_CODEC, DayZInventoryOpenData::pos,
            DayZInventoryOpenData::new
        );

    /** Opening the player's own inventory: no container, no position. */
    public static DayZInventoryOpenData none() {
        return new DayZInventoryOpenData(false, 0, false, BlockPos.ZERO);
    }

    /** Opening a nearby container. */
    public static DayZInventoryOpenData forContainer(int containerSize, BlockPos pos) {
        return new DayZInventoryOpenData(true, containerSize, true, pos);
    }
}
