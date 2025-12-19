package dev.dong.cerg.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
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
        if (originBlock instanceof BeltBlock) {
            encaseBelt(event);
            return;
        }

        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();
        Player player = event.getEntity();
        var ray = event.getHitVec();
        var hand = event.getHand();

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
        }// end for: connected

    }

    private static void encaseBelt(PlayerInteractEvent.RightClickBlock event) {
        BlockPos originPos = event.getPos();
        Player player = event.getEntity();
        Level world = event.getLevel();
        ItemStack heldItemStack = event.getItemStack();
        Item heldItem = heldItemStack.getItem();

        boolean isBrass = AllBlocks.BRASS_CASING.is(heldItem);
        if (!isBrass && !AllBlocks.ANDESITE_CASING.is(heldItem)) return;

        SoundType soundType = (isBrass ? AllBlocks.BRASS_CASING : AllBlocks.ANDESITE_CASING)
                .getDefaultState()
                .getSoundType(world, originPos, player);
        List<BlockPos> chain = BeltBlock.getBeltChain(world, BeltHelper.getControllerBE(world, originPos).getBlockPos());
        if (chain.isEmpty()) return;

        world.playSound(null, originPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        for (BlockPos p : chain) {
            BlockState s = world.getBlockState(p);
            var b = (BeltBlock) s.getBlock();
            b.withBlockEntityDo(world, p, be -> be.setCasingType(isBrass ? BRASS : ANDESITE));
            b.updateCoverProperty(world, p, s);
        }
        event.setCanceled(true);
    }

    protected static void chainDecase(PlayerInteractEvent.RightClickBlock event) {
        BlockPos originPos = event.getPos();
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState originState = world.getBlockState(pos);
        Block originBlock = originState.getBlock();
        boolean isCrouching = event.getEntity().isCrouching();

        // 传送带拆壳
        if (originBlock instanceof BeltBlock) {
            if (isCrouching) return;
            BeltBlockEntity beltCtrl = BeltHelper.getControllerBE(world, originPos);
            if (beltCtrl == null) return;

            List<BlockPos> chain = BeltBlock.getBeltChain(world, beltCtrl.getBlockPos());
            if (chain.isEmpty()) return;

            for (BlockPos p : chain) {
                BlockState s = world.getBlockState(p);
                var b = (BeltBlock) s.getBlock();
                b.withBlockEntityDo(world, p, be -> be.setCasingType(BeltBlockEntity.CasingType.NONE));
            }
            event.setCanceled(true);
            return;
        }

        // 传动方块拆壳

        if (!isCrouching) return;
        // 获取相连方块
    }
}
