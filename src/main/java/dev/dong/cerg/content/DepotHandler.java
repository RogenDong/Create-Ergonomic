package dev.dong.cerg.content;

import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.dong.cerg.mixin.tools.DepotBehaviourAccessor;
import dev.dong.cerg.util.LangUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * 置物台行为相关
 */
public class DepotHandler {
    /**
     * 置物台合并切换间隔（毫秒）
     */
    private static final long SWITCH_DEPOT_MERGE_DELAY = 500;
    /**
     * 上次切换置物台合并的时间
     */
    private static long lastSwitchDepotMergeTime = 0;

    /**
     * 切换普通置物台的合并功能
     */
    protected static void switchDepotMerge(PlayerInteractEvent.RightClickBlock event) {
        var now = System.currentTimeMillis();
        if (now - lastSwitchDepotMergeTime < SWITCH_DEPOT_MERGE_DELAY) return;

        var pos = event.getPos();
        var world = event.getLevel();
        var blockState = world.getBlockState(pos);
        if (blockState.isAir() || !(blockState.getBlock() instanceof DepotBlock)) return;

        var behaviour = BlockEntityBehaviour.get(world, pos, com.simibubi.create.content.logistics.depot.DepotBehaviour.TYPE);
        if (behaviour == null) return;
        lastSwitchDepotMergeTime = now;
        var player = event.getEntity();

        if (behaviour.canMergeItems()) ((DepotBehaviourAccessor) behaviour).setAllowMerge(false);
        else behaviour.enableMerging();

        var turnOn = behaviour.canMergeItems();
        world.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, .5f, turnOn ? .7f : .5f);
        LangUtil.translate("message.depot_merge")
                .add(LangUtil.enabled(turnOn))
                .sendStatus(player);
        behaviour.blockEntity.notifyUpdate();
    }
}
