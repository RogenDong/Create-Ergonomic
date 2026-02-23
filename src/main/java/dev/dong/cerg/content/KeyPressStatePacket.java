package dev.dong.cerg.content;

import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.CErgPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record KeyPressStatePacket(boolean isPressed, int keybind) implements ServerboundPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, KeyPressStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, KeyPressStatePacket::isPressed,
            ByteBufCodecs.INT, KeyPressStatePacket::keybind,
            KeyPressStatePacket::new
    );

    // 自定义构造器
    public KeyPressStatePacket(CErgKeys keybind) {
        this(keybind.isDown(), keybind.ordinal());
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        var key = CErgKeys.indexOf(this.keybind);
        if (key == null) return;

        PlayerKeyStates.setKeyState(player, key, isPressed);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CErgPackets.KEY_PRESS_STATE;
    }
}
