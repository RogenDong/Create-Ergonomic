package dev.dong.cerg.content;

import com.google.common.base.Objects;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Iterate;
import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.util.LangUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

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
            if (clusterCooldown == 15)
                player.displayClientMessage(Components.immutableEmpty(), true);
            CreateClient.OUTLINER.keep(clusterOutlineSlot);
            clusterCooldown--;
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
        if (firstPos != null && !firstPos.closerThan(hovered, 24)) {
            LangUtil.translate("clipboard_selection.too_far")
                    .color(FAIL)
                    .sendStatus(player);
            CreateClient.OUTLINER.keep(bbOutlineSlot);
            return;
        }

        if (firstPos != null && Objects.equal(hovered, hoveredPos)) {
            boolean cancel = player.isShiftKeyDown();
            if (!Objects.equal(firstPos, hovered)) {
                var color = cancel ? HIGHLIGHT : SUCCESS;
                var key = cancel
                        ? "clipboard_selection.click_to_discard"
                        : "clipboard_selection.click_to_confirm";

                LangUtil.translate(key, Component.keybind(CErgKeys.CHAIN_ENCASE.getName()))
                        .color(color)
                        .sendStatus(player);
            }

            AABB currentSelectionBox = getCurrentSelectionBox();
            if (currentSelectionBox != null)
                CreateClient.OUTLINER.showAABB(bbOutlineSlot, currentSelectionBox)
                        .colored(cancel ? FAIL : HIGHLIGHT)
                        .withFaceTextures(AllSpecialTextures.CUTOUT_CHECKERED, AllSpecialTextures.CUTOUT_CHECKERED)
                        .disableLineNormals()
                        .lineWidth(1 / 16f);
            return;
        }

        hoveredPos = hovered;
    }

    private AABB getCurrentSelectionBox() {
        return firstPos == null || hoveredPos == null
                ? null
                : new AABB(firstPos, hoveredPos).expandTowards(1, 1, 1);
    }

    public boolean onMouseInput() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;

        if (!player.mayBuild()) return false;
        if (!CLIPBOARD.isIn(player.getMainHandItem())) return false;
        var copied = player.getMainHandItem().getTagElement("CopiedValues");
        if (copied == null) {
            LangUtil.translate("clipboard_selection.have_not_copied")
                    .color(HIGHLIGHT)
                    .sendStatus(player);
            return true;
        }

        if (player.isShiftKeyDown()) {
            if (firstPos != null) {
                discard();
                return true;
            }
            return false;
        }

        if (hoveredPos == null) return false;

        if (firstPos != null) {
            confirm();
            return true;
        }

        firstPos = hoveredPos;
        LangUtil.translate("clipboard_selection.first_pos")
                .sendStatus(player);
        AllSoundEvents.CLIPBOARD_CHECKMARK.playAt(level, firstPos, 0.5F, 0.85F, false);
        level.playSound(player, firstPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
        return true;
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

        // The settings have been applied to all Cloneable blocks within the selected area.
//        AllPackets.getChannel().sendToServer(null);// TODO packet(firstPos, hoveredPos)
        AllSoundEvents.CLIPBOARD_ERASE.playAt(mc.level, hoveredPos, 0.5F, 0.95F, false);
        mc.level.playSound(player, hoveredPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);

        var currentSelectionBox = getCurrentSelectionBox();
        if (currentSelectionBox != null) {
            CreateClient.OUTLINER.showAABB(clusterOutlineSlot, currentSelectionBox)
                    .colored(0xB5F2C6)
                    .withFaceTextures(AllSpecialTextures.CUTOUT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
                    .disableLineNormals()
                    .lineWidth(1 / 24f);
            LangUtil.text("可粘贴方块数量: " + countCCBsWithSelection(currentSelectionBox))
                    .sendChat(mc.player);
        }

        discard();
        LangUtil.translate("clipboard_selection.success")
                .sendStatus(player);
        clusterCooldown = 30;
    }

    // ClipboardCloneable Blocks
    public static int countCCBsWithSelection(AABB aabb) {
        var mc = Minecraft.getInstance();
        var copied = mc.player.getMainHandItem().getTagElement("CopiedValues");
        if (copied == null) return 0;

        var level = mc.level;
        int count = 0;

        for (int y = (int) aabb.minY; y < aabb.maxY; y++) {
            for (int x = (int) aabb.minX; x < aabb.maxX; x++) {
                for (int z = (int) aabb.minZ; z < aabb.maxZ; z++) {
                    var offset = new BlockPos(x, y, z);
                    var state = level.getBlockState(offset);
                    if (!state.isAir() && canPaste(offset, copied)) count++;
                }
            }
        }
        return count;
    }

    public static boolean canPaste(BlockPos pos, CompoundTag copied) {
        var mc = Minecraft.getInstance();
        if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smartBE)) return false;

        for (var b : smartBE.getAllBehaviours())
            if (b instanceof ClipboardCloneable cc && testPaste(cc, copied))
                return true;

        return smartBE instanceof ClipboardCloneable cc && testPaste(cc, copied);
    }

    private static boolean testPaste(ClipboardCloneable cc, CompoundTag copied) {
        var player = Minecraft.getInstance().player;
        var ct = copied.getCompound(cc.getClipboardKey());
        for (Direction face : Iterate.directions)
            if (cc.readFromClipboard(ct, player, face, true))
                return true;
        return false;
    }

    /**
     * 选区操作过程中，没按住连锁按键，
     * 若此时发起潜伏右键，则取消事件
     */
    public boolean sneakClickWhenSelecting() {
        var stack = Minecraft.getInstance().player.getMainHandItem();
        return !stack.isEmpty() && CLIPBOARD.isIn(stack);
    }
}
