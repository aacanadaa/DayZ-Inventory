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
package com.suoim.dayzinventory.neoforge.network;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.DayZInventoryPayload;
import com.suoim.dayzinventory.neoforge.DayZInventoryNeoForge;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the mod's client-to-server payload.
 * <p>
 * NeoForge 1.21 uses the vanilla typed-payload system, so this replaces the old
 * Forge {@code SimpleChannel} entirely - there is no custom envelope class and
 * no manual encode/decode pair. The payload itself is shared with the Fabric
 * module.
 */
@EventBusSubscriber(modid = DayZInventoryNeoForge.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
            DayZInventoryPayload.TYPE,
            DayZInventoryPayload.CODEC,
            ModNetwork::handle
        );
    }

    private static void handle(final DayZInventoryPayload payload, final IPayloadContext context) {
        // The handler may run on the network thread, and DayZInventoryPackets
        // reads the buffer before scheduling its own work, so read it here.
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
        context.enqueueWork(() -> {
            // IPayloadContext#player is typed as Player; a playToServer payload
            // always arrives from a ServerPlayer.
            if (context.player() instanceof ServerPlayer serverPlayer) {
                DayZInventoryPackets.handlePacketOnServer(payload.packetId(), serverPlayer, buf);
            }
        });
    }
}
