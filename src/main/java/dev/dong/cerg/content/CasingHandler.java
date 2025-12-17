package dev.dong.cerg.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.*;
public class CasingHandler {

    protected static void chainEncase(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState originState = level.getBlockState(pos);
        Block originBlock = originState.getBlock();
        if (originBlock instanceof BeltBlock belt) {
            isBeltDo(event, originState, belt);
            return;
        }

        Player player = event.getEntity();
        var ray = event.getHitVec();
        var hand = event.getHand();
        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();
        // TODO 扳手拆机壳 Remove casing with wrench
        boolean encaseOrNot = false;

        // TODO 获取相连方块 get connected block
        List<BlockPos> connected = List.of(pos.east(), pos.south(), pos.west(), pos.north());

        for (BlockPos workmatePos : connected) {
            BlockState workmateState = level.getBlockState(workmatePos);
            Block workmate = workmateState.getBlock();
            if (!(workmate instanceof EncasableBlock)) continue;

            for (Block tmp : EncasingRegistry.getVariants(workmate)) {
                if (!(tmp instanceof EncasedBlock encased)) continue;
                if (encased.getCasing().asItem() != heldItem) continue;
                encased.handleEncasing(workmateState, level, workmatePos, heldItemStack, player, hand, ray);
                break;
            }
        }
    }

    private static void isBeltDo(PlayerInteractEvent.RightClickBlock event, BlockState originState, BeltBlock originBelt) {
        // TODO 扳手拆机壳 Remove casing with wrench
        boolean encaseOrNot = originState.getValue(BeltBlock.CASING);
        BlockPos originPos = event.getPos();
        Player player = event.getEntity();
        Level world = event.getLevel();
        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();

        boolean isBrass = AllBlocks.BRASS_CASING.is(heldItem);
        if (!isBrass && !AllBlocks.ANDESITE_CASING.is(heldItem)) return;

        List<BlockPos> chain = BeltBlock.getBeltChain(world, BeltHelper.getControllerBE(world, originPos).getBlockPos());
        SoundType soundType = (isBrass ? AllBlocks.BRASS_CASING : AllBlocks.ANDESITE_CASING)
                .getDefaultState()
                .getSoundType(world, originPos, player);

        for (BlockPos p : chain) {
            BlockState s = world.getBlockState(p);
            var b = (BeltBlock) s.getBlock();
            b.withBlockEntityDo(world, p, be -> be.setCasingType(isBrass ? BRASS : ANDESITE));
            b.updateCoverProperty(world, p, s);
        }
        world.playSound(null, originPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        event.setCanceled(true);
    }
}
