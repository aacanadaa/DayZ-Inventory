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
package com.suoim.dayzinventory.forge.network;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.DayZInventoryPayload;
import com.suoim.dayzinventory.forge.DayZInventoryForge;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

/**
 * Registers the mod's client-to-server payload.
 * <p>
 * Forge 1.21 uses the vanilla typed-payload system, as NeoForge does, so the
 * shared {@link DayZInventoryPayload} is reused as-is. This is nonetheless a
 * rewrite rather than a port of the 1.20.1 module, which was built on
 * {@code SimpleChannel.registerMessage} - an API that no longer exists, along
 * with {@code NetworkRegistry.newSimpleChannel} and
 * {@code net.minecraftforge.network.NetworkEvent}.
 * <p>
 * The channel is built through {@link ChannelBuilder#payloadChannel()}. The
 * {@code PayloadChannel} that name suggests is package-private, so the built
 * {@link Channel} is what this class keeps hold of.
 */
public class ModNetwork {

    private static Channel<CustomPacketPayload> channel;

    public static void register() {
        var connection = ChannelBuilder
            .named(Identifier.fromNamespaceAndPath(DayZInventoryForge.MOD_ID, "main"))
            .networkProtocolVersion(1)
            .payloadChannel();

        // The PLAY protocol only. serverbound() narrows the flow to
        // client -> server and returns the PayloadFlow that payloads are added
        // to; build() then locks the channel and must come last, once.
        var flow = connection.play().serverbound();

        flow.add(DayZInventoryPayload.TYPE, DayZInventoryPayload.CODEC, ModNetwork::handle);

        channel = flow.build();
    }

    /**
     * Runs on the network thread, so it must not touch game state directly. The
     * buffer is rebuilt here because {@link DayZInventoryPackets} reads it before
     * scheduling its own work.
     */
    private static void handle(DayZInventoryPayload payload, CustomPayloadEvent.Context context) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                DayZInventoryPackets.handlePacketOnServer(payload.packetId(), sender, buf);
            }
        });

        context.setPacketHandled(true);
    }

    public static void sendToServer(DayZInventoryPayload payload) {
        // PacketDistributor.PacketTarget#send only accepts a vanilla Packet, so
        // the typed payload goes through the channel instead.
        channel.send(payload, PacketDistributor.SERVER.noArg());
    }
}
