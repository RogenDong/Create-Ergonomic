package dev.dong.cerg.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * 玩家交互
 */
public class PlayerInteract {
    /**
     * 监听玩家右键
     */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide || player == null) return;
        if (player.isShiftKeyDown() || !player.mayBuild()) return;

        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();
        if (heldItem instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof CasingBlock) {
                CasingHandler.chainEncase(event);
            }
        } else if (AllItems.WRENCH.is(heldItem)) {
            BlockPos pos = event.getPos();
            BlockState originState = level.getBlockState(pos);
            Block originBlock = originState.getBlock();

            if (AllBlocks.DEPOT.is(originBlock)) DepotHandler.switchDepotMerge(event);
            else CasingHandler.chainDecase(event);
        }
        // TODO 水车材质替换
    }
}
