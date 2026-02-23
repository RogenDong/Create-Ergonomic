package dev.dong.cerg.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import dev.dong.cerg.CErg;
import dev.dong.cerg.CErgClient;
import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.content.CasingHandler;
import dev.dong.cerg.content.DepotHandler;
import dev.dong.cerg.content.PipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

import static dev.dong.cerg.CErgKeys.CHAIN_ENCASE;
import static dev.dong.cerg.content.PlayerKeyStates.isKeyPressed;
import static com.simibubi.create.AllItems.WRENCH;
import static com.simibubi.create.AllBlocks.CLIPBOARD;
import static com.simibubi.create.AllBlocks.FLUID_PIPE;
import static com.simibubi.create.AllBlocks.ENCASED_FLUID_PIPE;

/**
 * 玩家交互
 */
public class PlayerInteract {

    /**
     * 鼠标交互（客户端）
     */
    public static void onClientClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        var key = event.getKeyMapping();

        boolean isAtk = key == mc.options.keyAttack;
        if ((isAtk || key == mc.options.keyUse)
                && CErgKeys.CHAIN_ENCASE.isDown()
                && event.getHand() == InteractionHand.MAIN_HAND
                && CLIPBOARD.isIn(mc.player.getMainHandItem())) {
            event.setCanceled(true);
            CErgClient.CLIPBOARD_HANDLER.onMouseInput(isAtk);
        }
    }

    /**
     * 监听玩家右键
     * <ul>
     *   <li>客户端、服务端各触发一次</li>
     *   <li>左右手各触发一次</li>
     * </ul>
     */
    public static void rightClick(RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockState originState = level.getBlockState(event.getPos());
        if (originState.isAir()) return;

        Player player = event.getEntity();
        if (player == null || !player.mayBuild()) return;

        ItemStack heldItemStack = event.getItemStack();

        //-----------------
        // 不需要按键的……
        //-----------------

        if (!isKeyPressed(player, CHAIN_ENCASE)) {
            // 右手空空
            if (heldItemStack.isEmpty()
                    && event.getHand() == InteractionHand.MAIN_HAND
                    && (FLUID_PIPE.has(originState) || ENCASED_FLUID_PIPE.has(originState))) {
                PipeHandler.togglePipeConnection(event);
                return;
            }
            Block originBlock = originState.getBlock();
            // 切换置物台合并物品开关
            if (CErg.CONFIG.general.enableDepotMerge) {
                if (WRENCH.isIn(heldItemStack) && AllBlocks.DEPOT.is(originBlock) && !player.isCrouching()) {
                    DepotHandler.switchDepotMerge(event);
                    return;
                }
            }
            return;
        }

        //-----------------
        // 需要按住连锁键的……
        //-----------------

        Item heldItem = heldItemStack.getItem();

        // 管道连锁开窗
        if (CErg.CONFIG.general.enableChainTogglePipes) {
            if (WRENCH.is(heldItem) && PipeHandler.isAxialPipe(originState)) {
                PipeHandler.chainTogglePipe(event);
                return;
            }
        }

        if (CErg.CONFIG.general.enableChainEncase) {
            // 连锁套壳
            if (heldItem instanceof BlockItem bi && bi.getBlock() instanceof CasingBlock) {
                CasingHandler.chainEncase(event);
                return;
            }

            // 连锁拆壳
            if (WRENCH.is(heldItem)) {
                CasingHandler.chainDecase(event);
                return;
            }
        }

        // TODO 水车材质替换
    }
}
