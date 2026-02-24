package dev.dong.cerg.content;

import com.google.common.base.Objects;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllSpecialTextures;
import dev.dong.cerg.CErg;
import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.CErgPackets;
import dev.dong.cerg.util.LangUtil;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

import java.util.Optional;

import static com.simibubi.create.AllBlocks.CLIPBOARD;

public class ClipboardSelectionHandler {

    private static final int SUCCESS = 0x68c586;
    private static final int HIGHLIGHT = 0xc5b548;
    private static final int FAIL = 0xff5d6c;

    private final Object clusterOutlineSlot = new Object();
    private final Object bbOutlineSlot = new Object();
    private int clusterCooldown;

    private BlockPos firstPos;
    private BlockPos hoveredPos;

    public void tick() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        BlockPos hovered = null;
        var stack = player.getMainHandItem();

        // 若切换了物品则丢弃选区
        if (!CLIPBOARD.isIn(stack)) {
            if (firstPos != null)
                discard();
            return;
        }

        // 淡出高亮选区
        if (clusterCooldown > 0) {
            if (clusterCooldown == 25)
                player.displayClientMessage(Component.empty(), true);
            Outliner.getInstance().keep(clusterOutlineSlot);
            clusterCooldown--;
        }

        // 剪贴板需要保持复制的配置
        var copied = ClipboardSelectionHelper.getCopiedData(player.getMainHandItem());
        if (firstPos != null && copied.isEmpty()) {
            discard();
            return;
        }

        // 获取瞄准方块位置
        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == Type.BLOCK)
            hovered = ((BlockHitResult) hitResult).getBlockPos();

        if (hovered == null) {
            hoveredPos = null;
            return;
        }

        // 限制选区范围
        if (firstPos != null && !firstPos.closerThan(hovered, 21.5)) {
            LangUtil.translate("clipboard_selection.too_far")
                    .color(FAIL)
                    .sendStatus(player);
            Outliner.getInstance().keep(bbOutlineSlot);
            return;
        }

        if (firstPos != null && Objects.equal(hovered, hoveredPos)) {
            boolean cancel = player.isShiftKeyDown();
            if (!Objects.equal(firstPos, hovered)) {
                var color = cancel ? HIGHLIGHT : SUCCESS;
                var key = cancel
                        ? "clipboard_selection.click_to_discard"
                        : "clipboard_selection.click_to_confirm";

                LangUtil.translate(key,
                                Component.keybind(CErgKeys.CHAIN_ENCASE.getName()),
                                Component.keybind("key.attack"))
                        .color(color)
                        .sendStatus(player);
            }

            AABB currentSelectionBox = getCurrentSelectionBox();
            if (currentSelectionBox != null)
                Outliner.getInstance().showAABB(bbOutlineSlot, currentSelectionBox)
                        .colored(cancel ? FAIL : HIGHLIGHT)
                        .withFaceTextures(AllSpecialTextures.CUTOUT_CHECKERED, AllSpecialTextures.CUTOUT_CHECKERED)
                        .disableLineNormals()
                        .lineWidth(1 / 16f);
            return;
        }

        hoveredPos = hovered;
    }

    private AABB getCurrentSelectionBox() {
        if (firstPos == null || hoveredPos == null) return null;
        return new AABB(firstPos.getCenter(), hoveredPos.getCenter()).expandTowards(1, 1, 1);
    }

    public void onMouseInput(boolean isAtk) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        var held = player.getMainHandItem();

        if (!player.mayBuild()) return;
        if (!CLIPBOARD.isIn(held)) return;
        if (ClipboardSelectionHelper.getCopiedData(held).isEmpty()) {
            LangUtil.translate("clipboard_selection.have_not_copied")
                    .color(HIGHLIGHT)
                    .sendStatus(player);
            return;
        }

        if (hoveredPos == null) return;

        // 左键
        if (isAtk) {
            if (firstPos != null) {
                // 取消选择
                if (player.isShiftKeyDown()) discard();
                    // 确认选区第二点
                else confirm();
            }
            return;
        }

        // 确认选区第一点（右键）
        firstPos = hoveredPos;
        LangUtil.translate("clipboard_selection.first_pos")
                .sendStatus(player);
        AllSoundEvents.CLIPBOARD_CHECKMARK.playAt(level, firstPos, 0.5F, 0.85F, false);
        level.playSound(player, firstPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
    }

    public void discard() {
        LocalPlayer player = Minecraft.getInstance().player;
        firstPos = null;
        LangUtil.translate("clipboard_selection.abort")
                .sendStatus(player);
        clusterCooldown = 0;
    }

    public void confirm() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var currentSelectionBox = getCurrentSelectionBox();
        int countCCB = countCCBsWithSelection(firstPos, hoveredPos);
        CErg.LOGGER.info("count clipboard-cloneable block: {}", countCCB);

        Outliner.getInstance().showAABB(clusterOutlineSlot, currentSelectionBox)
                .colored(0xB5F2C6)
                .withFaceTextures(AllSpecialTextures.CUTOUT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
                .disableLineNormals()
                .lineWidth(1 / 24f);

        AllSoundEvents.CLIPBOARD_ERASE.playAt(mc.level, hoveredPos, 0.5F, 0.95F, false);
        mc.level.playSound(player, hoveredPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);

        if (countCCB > 0) CErgPackets.sendToServer(
                new ClipboardBatchPastePacket(player.getInventory().selected, firstPos, hoveredPos));

        discard();
        LangUtil.translate("clipboard_selection.success", countCCB)
                .sendChat(player);
        clusterCooldown = 40;
    }

    private static int countCCBsWithSelection(BlockPos firstPos, BlockPos secondPos) {
        if (firstPos == null || secondPos == null) return 0;

        var mc = Minecraft.getInstance();
        var copied = ClipboardSelectionHelper.getCopiedData(mc.player.getMainHandItem());
        if (copied.isEmpty()) return 0;
        var values = copied.get();

        var minX = Math.min(firstPos.getX(), secondPos.getX());
        var minY = Math.min(firstPos.getY(), secondPos.getY());
        var minZ = Math.min(firstPos.getZ(), secondPos.getZ());
        var maxX = Math.max(firstPos.getX(), secondPos.getX());
        var maxY = Math.max(firstPos.getY(), secondPos.getY());
        var maxZ = Math.max(firstPos.getZ(), secondPos.getZ());
        int count = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var offset = new BlockPos(x, y, z);
                    var state = mc.level.getBlockState(offset);
                    if (!state.isAir() && ClipboardSelectionHelper.tryPaste(
                            mc.level, mc.player, offset, values, true))
                        count++;
                }
            }
        }
        return count;
    }

}
