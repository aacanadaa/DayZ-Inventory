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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server envelope.
 * <p>
 * Before 1.20.5 this mod sent a bare {@code FriendlyByteBuf} under a
 * {@link Identifier} channel. Custom payloads are now typed records with a
 * {@link StreamCodec}, so the same "channel id + raw bytes" shape is carried
 * explicitly here and the existing dispatch logic in {@link DayZInventoryPackets}
 * keeps working unchanged.
 */
public record DayZInventoryPayload(Identifier packetId, byte[] data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DayZInventoryPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("dayz_inventory", "main"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DayZInventoryPayload> CODEC =
        StreamCodec.composite(
            Identifier.STREAM_CODEC, DayZInventoryPayload::packetId,
            ByteBufCodecs.BYTE_ARRAY, DayZInventoryPayload::data,
            DayZInventoryPayload::new
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
