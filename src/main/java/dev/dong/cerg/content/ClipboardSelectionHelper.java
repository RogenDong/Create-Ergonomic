package dev.dong.cerg.content;

import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Iterate;
import dev.dong.cerg.CErg;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/**
 * 剪贴板选区数据处理工具
 */
public class ClipboardSelectionHelper {

    public static boolean tryPaste(Level level, Player player, BlockPos pos, CompoundTag copied, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE)) return false;

        for (var b : smartBE.getAllBehaviours())
            if (b instanceof ClipboardCloneable cc && testPaste(player, cc, copied, simulate))
                return true;

        return smartBE instanceof ClipboardCloneable cc && testPaste(player, cc, copied, simulate);
    }

    private static boolean testPaste(Player player, ClipboardCloneable cc, CompoundTag copied, boolean simulate) {
        var ct = copied.getCompound(cc.getClipboardKey());
        for (var face : Iterate.directions)
            if (cc.readFromClipboard(ct, player, face, simulate))
                return true;
        return false;
    }

    public static void batchPaste(Level level, Player player, ClipboardBatchPastePacket data) {
        if (data.firstPos == null || data.secondPos == null) return;

        var inventory = player.getInventory();
        if (inventory.selected != data.selectedSlot) return;

        var copied = inventory.getSelected().getTagElement("CopiedValues");
        if (copied == null) return;

        var uid = player.getStringUUID();
        var list = new ArrayList<BlockPos>();
        CErg.LOGGER.debug("batch paste by {}({})", player.getName().getString(), uid);
        CErg.LOGGER.debug("[{}] applying settings: {}", uid, copied);

        int minX = Math.min(data.firstPos.getX(), data.secondPos.getX());
        int minY = Math.min(data.firstPos.getY(), data.secondPos.getY());
        int minZ = Math.min(data.firstPos.getZ(), data.secondPos.getZ());
        int maxX = Math.max(data.firstPos.getX(), data.secondPos.getX());
        int maxY = Math.max(data.firstPos.getY(), data.secondPos.getY());
        int maxZ = Math.max(data.firstPos.getZ(), data.secondPos.getZ());

        // TODO 统计玩家过滤器数量，提前结束搜索
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var offset = new BlockPos(x, y, z);
                    var state = level.getBlockState(offset);
                    if (!state.isAir() && tryPaste(level, player, offset, copied, false))
                        list.add(offset);
                }
            }
        }

        if (list.isEmpty()) return;

        var tmp = new StringBuilder();
        for (var p : list)
            tmp.append(String.format("(%d,%d,%d)", p.getX(), p.getY(), p.getZ()));
        CErg.LOGGER.debug("[{}] {} CC Blocks applied: [{}]", uid, list.size(), tmp);
    }
}
