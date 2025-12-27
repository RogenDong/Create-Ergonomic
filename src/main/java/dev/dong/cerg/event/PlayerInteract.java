package dev.dong.cerg.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import dev.dong.cerg.content.CasingHandler;
import dev.dong.cerg.content.DepotHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import static dev.dong.cerg.CErgKeys.CHAIN_ENCASE;
import static dev.dong.cerg.content.PlayerKeyStates.isKeyPressed;

/**
 * 玩家交互
 */
public class PlayerInteract {
    /**
     * 监听玩家右键
     */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockState originState = level.getBlockState(event.getPos());
        if (originState.isAir()) return;

        Player player = event.getEntity();
        if (player == null || !player.mayBuild() || !isKeyPressed(player, CHAIN_ENCASE)) return;

        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();
        boolean isCrouching = player.isCrouching();

        // 使用机壳
        if (heldItem instanceof BlockItem blockItem) {
            if (isCrouching) return;
            // 连锁套壳
            if (blockItem.getBlock() instanceof CasingBlock) {
                CasingHandler.chainEncase(event);
            }
            return;
        }

        // 使用扳手
        if (!AllItems.WRENCH.is(heldItem)) return;
        Block originBlock = originState.getBlock();

        // 切换置物台合并物品开关 // 连锁拆壳
        if (AllBlocks.DEPOT.is(originBlock) && !isCrouching) DepotHandler.switchDepotMerge(event);
        else CasingHandler.chainDecase(event);

        // TODO 管道连锁开窗

        // TODO 水车材质替换
    }
}
