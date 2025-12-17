package dev.dong.cerg.content;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.dong.cerg.mixin.tools.DepotBehaviourAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * 玩家交互
 */
public class PlayerInteract {
    /**
     * 置物台合并切换间隔（毫秒）
     */
    private static final long SWITCH_DEPOT_MERGE_DELAY = 500;
    /**
     * 上次切换置物台合并的时间
     */
    private static long lastSwitchDepotMergeTime = 0;

    /**
     * 监听玩家右键
     */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.CLIENT) return;
        if (event.getEntity() == null) return;

        var item = event.getItemStack().getItem();
        if (item instanceof WrenchItem) {
            switchDepotMerge(event);
//        } else if (CasingBlock) {
        }
    }

    /**
     * 切换普通置物台的合并功能
     */
    private static void switchDepotMerge(PlayerInteractEvent.RightClickBlock event) {
        var now = System.currentTimeMillis();
        if (now - lastSwitchDepotMergeTime < SWITCH_DEPOT_MERGE_DELAY) return;

        var pos = event.getPos();
        var world = event.getLevel();
        var blockState = world.getBlockState(pos);
        if (blockState.isAir() || !(blockState.getBlock() instanceof DepotBlock)) return;

        var behaviour = BlockEntityBehaviour.get(world, pos, DepotBehaviour.TYPE);
        if (behaviour == null) return;
        lastSwitchDepotMergeTime = now;
        var player = event.getEntity();

        if (behaviour.canMergeItems()) {
            ((DepotBehaviourAccessor) behaviour).setAllowMerge(false);
            world.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, .5f, .5f);
            player.displayClientMessage(Component.literal("已关闭物品合并功能"), true);
        } else {
            behaviour.enableMerging();
            world.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, .5f, .7f);
            player.displayClientMessage(Component.literal("已启用物品合并功能").withStyle(ChatFormatting.GREEN), true);
        }
        behaviour.blockEntity.notifyUpdate();
    }
}
