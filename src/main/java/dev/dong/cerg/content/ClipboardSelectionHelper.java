package dev.dong.cerg.content;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.createmod.catnip.data.Iterate;
import dev.dong.cerg.CErg;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Optional;

/**
 * 剪贴板选区数据处理工具
 */
public class ClipboardSelectionHelper {

    /**
     * 获取剪贴板复制的过滤数据
     */
    public static Optional<CompoundTag> getCopiedData(ItemStack stack) {
        var c = stack.get(AllDataComponents.CLIPBOARD_CONTENT);
        return c == null ? Optional.empty() : c.copiedValues();
    }

    public static boolean tryPaste(Level level, Player player, BlockPos pos, CompoundTag copied, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE)) return false;

        if (smartBE instanceof ClipboardCloneable cc && testPaste(level, player, cc, copied, simulate))
            return true;

        for (var b : smartBE.getAllBehaviours())
            if (b instanceof ClipboardCloneable cc && testPaste(level, player, cc, copied, simulate))
                return true;

        return false;
    }

    private static boolean testPaste(Level level, Player player, ClipboardCloneable cc, CompoundTag copied, boolean simulate) {
        var ins = copied.getCompound(cc.getClipboardKey());
        var reg = level.registryAccess();
        for (var face : Iterate.directions) {
            if (cc.readFromClipboard(reg, copied, player, face, simulate))
                return true;
            if (cc.readFromClipboard(reg, ins, player, face, simulate))
                return true;
        }
        return false;
    }

    public static void batchPaste(Level level, Player player, ClipboardBatchPastePacket data) {
        var firstPos = data.firstPos();
        var secondPos = data.secondPos();
        if (firstPos == null || secondPos == null) return;

        var inventory = player.getInventory();
        if (inventory.selected != data.selectedSlot()) return;

        var copied = getCopiedData(inventory.getSelected());
        if (copied.isEmpty()) return;
        var values = copied.get();

        var uid = player.getStringUUID();
        var list = new ArrayList<BlockPos>();
        CErg.LOGGER.debug("batch paste by {}({})", player.getName().getString(), uid);
        CErg.LOGGER.debug("[{}] applying settings: {}", uid, values);

        int minX = Math.min(firstPos.getX(), secondPos.getX());
        int minY = Math.min(firstPos.getY(), secondPos.getY());
        int minZ = Math.min(firstPos.getZ(), secondPos.getZ());
        int maxX = Math.max(firstPos.getX(), secondPos.getX());
        int maxY = Math.max(firstPos.getY(), secondPos.getY());
        int maxZ = Math.max(firstPos.getZ(), secondPos.getZ());

        // TODO 统计玩家过滤器数量，提前结束搜索
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var offset = new BlockPos(x, y, z);
                    var state = level.getBlockState(offset);
                    if (!state.isAir() && tryPaste(level, player, offset, values, false))
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
