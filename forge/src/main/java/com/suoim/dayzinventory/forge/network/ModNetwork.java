package com.suoim.dayzinventory.forge.network;

import com.suoim.dayzinventory.DayZInventoryPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("dayz_inventory", "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, DayZInventoryPlatformPacket.class,
            DayZInventoryPlatformPacket::encode,
            DayZInventoryPlatformPacket::decode,
            DayZInventoryPlatformPacket::handle
        );
    }

    public static class DayZInventoryPlatformPacket {
        private final ResourceLocation packetId;
        private final FriendlyByteBuf data;

        public DayZInventoryPlatformPacket(ResourceLocation packetId, FriendlyByteBuf data) {
            this.packetId = packetId;
            this.data = data;
        }

        public static void encode(DayZInventoryPlatformPacket msg, FriendlyByteBuf buf) {
            buf.writeResourceLocation(msg.packetId);
            
            // Read bytes from FriendlyByteBuf
            int readableBytes = msg.data.readableBytes();
            byte[] bytes = new byte[readableBytes];
            msg.data.getBytes(msg.data.readerIndex(), bytes); // Use getBytes to avoid modifying indices
            buf.writeByteArray(bytes);
        }

        public static DayZInventoryPlatformPacket decode(FriendlyByteBuf buf) {
            ResourceLocation packetId = buf.readResourceLocation();
            byte[] bytes = buf.readByteArray();
            FriendlyByteBuf data = new FriendlyByteBuf(io.netty.buffer.Unpooled.copiedBuffer(bytes));
            return new DayZInventoryPlatformPacket(packetId, data);
        }

        public static void handle(DayZInventoryPlatformPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null) {
                    DayZInventoryPackets.handlePacketOnServer(msg.packetId, player, msg.data);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
