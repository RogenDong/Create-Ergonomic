package dev.dong.cerg;

import dev.dong.cerg.content.KeyPressStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public class CErgPackets {
    public static final ResourceLocation CHANNEL_NAME = CErg.asResource(CErg.ID);
    public static final int NETWORK_VERSION = 3;
    public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
    private static SimpleChannel channel;

    public static void registerPackets() {
        channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
                .serverAcceptedVersions(NETWORK_VERSION_STR::equals)
                .clientAcceptedVersions(NETWORK_VERSION_STR::equals)
                .networkProtocolVersion(() -> NETWORK_VERSION_STR)
                .simpleChannel();

        channel.messageBuilder(KeyPressStatePacket.class, 0, PLAY_TO_SERVER)
                .encoder(KeyPressStatePacket::write)
                .decoder(KeyPressStatePacket::new)
                .consumerNetworkThread((packet, contextSupplier) -> {
                    Context context = contextSupplier.get();
                    if (packet.handle(context)) context.setPacketHandled(true);
                })
                .add();
    }

    public static SimpleChannel getChannel() {
        return channel;
    }

    public static <MSG> void sendToServer(MSG packet) {
        var c1 = Minecraft.getInstance().getConnection();
        if (c1 != null) channel.sendToServer(packet);
    }

}
