package dev.dong.cerg.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import dev.dong.cerg.CErg;
import dev.dong.cerg.content.CasingHandler;
import dev.dong.cerg.content.DepotHandler;
import dev.dong.cerg.content.PipeHandler;
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
        if (player == null || !player.mayBuild()) return;

        //-----------------
        // 不需要按键的……
        //-----------------

        if (!isKeyPressed(player, CHAIN_ENCASE)) {
            Block originBlock = originState.getBlock();
            // 切换置物台合并物品开关
            if (CErg.CONFIG.general.enableDepotMerge && AllBlocks.DEPOT.is(originBlock) && !player.isCrouching())
                DepotHandler.switchDepotMerge(event);
            return;
        }

        //-----------------
        // 需要按住连锁键的……
        //-----------------

        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();

        // 管道连锁开窗
        if (CErg.CONFIG.general.enableChainTogglePipes
                && AllItems.WRENCH.is(heldItem) && PipeHandler.isAxialPipe(originState)) {
            PipeHandler.chainTogglePipe(event);
            return;
        }

        if (CErg.CONFIG.general.enableChainEncase) {
            // 连锁套壳
            if (heldItem instanceof BlockItem bi && bi.getBlock() instanceof CasingBlock) {
                CasingHandler.chainEncase(event);
                return;
            }

            // 连锁拆壳
            if (AllItems.WRENCH.is(heldItem)) {
                CasingHandler.chainDecase(event);
                return;
            }
        }

        // TODO 水车材质替换
    }
}
