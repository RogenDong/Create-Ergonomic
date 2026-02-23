package dev.dong.cerg.content;

import dev.dong.cerg.CErgPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * 剪贴板批量粘贴信号
 */
public record ClipboardBatchPastePacket(int selectedSlot, BlockPos firstPos, BlockPos secondPos) implements ServerboundPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ClipboardBatchPastePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClipboardBatchPastePacket::selectedSlot,
            BlockPos.STREAM_CODEC, ClipboardBatchPastePacket::firstPos,
            BlockPos.STREAM_CODEC, ClipboardBatchPastePacket::secondPos,
            ClipboardBatchPastePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (player == null) return;

        var level = player.level();
        if (level.isLoaded(firstPos) && level.isLoaded(secondPos))
            ClipboardSelectionHelper.batchPaste(level, player, this);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CErgPackets.CLIPBOARD_BATCH_PASTE;
    }
}
