package dev.dong.cerg.content;

import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
        if (event.getLevel().isClientSide || player == null) return;
        if (player.isShiftKeyDown() || !player.mayBuild()) return;

        ItemStack itemStack = event.getItemStack();
        var item = itemStack.getItem();
        if (item instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof CasingBlock) {
//                CasingHandler.chainEncase(event);
            }
        } else if (item instanceof WrenchItem) {
            DepotHandler.switchDepotMerge(event);
        }
    }
}
