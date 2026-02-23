package dev.dong.cerg;

import dev.dong.cerg.content.ClipboardBatchPastePacket;
import dev.dong.cerg.content.KeyPressStatePacket;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum CErgPackets implements BasePacketPayload.PacketTypeProvider {
    KEY_PRESS_STATE(KeyPressStatePacket.class, KeyPressStatePacket.STREAM_CODEC),
    CLIPBOARD_BATCH_PASTE(ClipboardBatchPastePacket.class, ClipboardBatchPastePacket.STREAM_CODEC)
    ;

    public static final int NETWORK_VERSION = 3;
    public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
    private final CatnipPacketRegistry.PacketType<?> type;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    <T extends BasePacketPayload> CErgPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(CErg.asResource(name)), clazz, codec);
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CErg.ID, NETWORK_VERSION_STR);
        for (CErgPackets packet : CErgPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }

    public static void sendToServer(ServerboundPacketPayload packet) {
        try {
            CatnipServices.NETWORK.sendToServer(packet);
        } catch (Exception e) {
            CErg.LOGGER.error("Failed to send packet to client", e);
        }
    }
}
