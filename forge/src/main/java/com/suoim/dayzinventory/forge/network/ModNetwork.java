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
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.20.6 {
import com.suoim.dayzinventory.DayZInventoryPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
//?} else {
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;
//?}

/**
 * Registers the mod's client-to-server channel.
 * <p>
 * Every message is the same envelope - a sub-packet id plus raw bytes - which
 * {@link DayZInventoryPackets} dispatches. Only the transport differs:
 * <ul>
 *   <li><b>1.20.6+</b>: the vanilla typed-payload system, through
 *       {@link ChannelBuilder#payloadChannel()}, reusing the
 *       {@code DayZInventoryPayload} shared with Fabric and NeoForge.</li>
 *   <li><b>1.20.1</b>: Forge's {@code SimpleChannel}, which predates typed
 *       payloads, with a hand-written encoder and decoder.</li>
 * </ul>
 */
public class ModNetwork {
//? if >=1.20.6 {
    private static Channel<CustomPacketPayload> channel;

    public static void register() {
        var connection = ChannelBuilder
            .named(DayZInventoryPackets.id("main"))
            .networkProtocolVersion(1)
            .payloadChannel();

        // The PLAY protocol only. serverbound() narrows the flow to
        // client -> server and returns the PayloadFlow that payloads are added
        // to; build() then locks the channel and must come last, once.
        var flow = connection.play().serverbound();
        flow.add(DayZInventoryPayload.TYPE, DayZInventoryPayload.CODEC, ModNetwork::handle);
        channel = flow.build();
    }

    // Runs on the network thread, so it must not touch game state directly. The
    // buffer is rebuilt here because DayZInventoryPackets reads it before
    // scheduling its own work.
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

    public static void sendToServer(Identifier packetId, byte[] data) {
        // PacketDistributor.PacketTarget#send only accepts a vanilla Packet, so
        // the typed payload goes through the channel instead.
        channel.send(new DayZInventoryPayload(packetId, data), PacketDistributor.SERVER.noArg());
    }
//?} else {
    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        DayZInventoryPackets.id("main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    // The 1.20.1 envelope: the same (sub-packet id, bytes) pair as the payload record.
    private record Envelope(Identifier packetId, byte[] data) {
        void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(this.packetId);
            buf.writeByteArray(this.data);
        }

        static Envelope decode(FriendlyByteBuf buf) {
            return new Envelope(buf.readResourceLocation(), buf.readByteArray());
        }
    }

    public static void register() {
        CHANNEL.registerMessage(0, Envelope.class, Envelope::encode, Envelope::decode, ModNetwork::handle);
    }

    private static void handle(Envelope message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(message.data()));
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                DayZInventoryPackets.handlePacketOnServer(message.packetId(), sender, buf);
            }
        });
        context.setPacketHandled(true);
    }

    public static void sendToServer(Identifier packetId, byte[] data) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), new Envelope(packetId, data));
    }
//?}
}
