package dev.dong.cerg.content;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.dong.cerg.mixin.tools.DepotBehaviourAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * 玩家交互
 */
public class PlayerInteract {
    private static final long SWITCH_DEPOT_MERGE_DELAY = 500;// ms
    private static long lastSwitchDepotMergeTime = 0;

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        var now = System.currentTimeMillis();
        if (now - lastSwitchDepotMergeTime < SWITCH_DEPOT_MERGE_DELAY) return;
        if (event.getSide() == LogicalSide.CLIENT) return;

        Player player = event.getEntity();
        if (player == null) return;
        if (!(event.getItemStack().getItem() instanceof WrenchItem)) return;

        BlockPos pos = event.getPos();
        Level world = event.getLevel();
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isAir() || !(blockState.getBlock() instanceof DepotBlock)) return;

        DepotBehaviour behaviour = BlockEntityBehaviour.get(world, pos, DepotBehaviour.TYPE);
        if (behaviour == null) return;

        lastSwitchDepotMergeTime = now;
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
