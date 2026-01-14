package dev.dong.cerg.content;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import dev.dong.cerg.CErg;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

/**
 * 剪贴板批量粘贴信号
 */
public class ClipboardBatchPastePacket extends SimplePacketBase {
    private static final ResourceLocation ID = CErg.asResource("clipboard_batch_paste");
    public final int selectedSlot;
    public final BlockPos firstPos;
    public final BlockPos secondPos;

    public ClipboardBatchPastePacket(FriendlyByteBuf buf) {
        this.selectedSlot = buf.readInt();
        this.firstPos = buf.readBlockPos();
        this.secondPos = buf.readBlockPos();
    }

    // 自定义构造器
    public ClipboardBatchPastePacket(int selectedSlot, BlockPos firstPos, BlockPos secondPos) {
        this.firstPos = firstPos;
        this.secondPos = secondPos;
        this.selectedSlot = selectedSlot;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(selectedSlot);
        buffer.writeBlockPos(firstPos);
        buffer.writeBlockPos(secondPos);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> batchPaste(context));
        return true;
    }

    private void batchPaste(Context context) {
        var player = context.getSender();
        if (player == null) return;

        var level = player.level();
        if (level.isLoaded(firstPos) && level.isLoaded(secondPos))
            ClipboardSelectionHelper.batchPaste(level, player, this);
    }
}
