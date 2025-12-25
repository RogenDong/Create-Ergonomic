package dev.dong.cerg.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import dev.dong.cerg.util.S2E;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

import java.util.List;
import java.util.function.Predicate;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.ANDESITE;
import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.BRASS;

public class CasingHandler {

    public static int MAX_CHAIN = 64;

    protected static void chainEncase(RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos originPos = event.getPos();
        BlockState originState = level.getBlockState(originPos);
        Block originBlock = originState.getBlock();
        if (originBlock instanceof BeltBlock) {
            encaseBelt(event);
            return;
        }

        ItemStack heldItemStack = event.getItemStack();
        Axis axis = originState.getValue(AXIS);
        chain(originPos, axis, pos -> tryEncase(event, pos, axis, heldItemStack));
    }

    private static boolean tryEncase(RightClickBlock e, BlockPos pos, Axis a, ItemStack held) {
        Level level = e.getLevel();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        Block block = state.getBlock();
        if (!(block instanceof RotatedPillarKineticBlock) || state.getValue(AXIS) != a) return false;

        EncasedBlock encased = getEncasedBlock(block, held.getItem());
        if (encased == null) return false;

        encased.handleEncasing(state, level, pos, held, e.getEntity(), e.getHand(), e.getHitVec());
        return true;
    }

    private static EncasedBlock getEncasedBlock(Block block, Item held) {
        if (block instanceof EncasableBlock)
            for (Block v : EncasingRegistry.getVariants(block))
                if (v instanceof EncasedBlock encased && encased.getCasing().asItem() == held)
                    return encased;

        return null;
    }

    private static void encaseBelt(RightClickBlock event) {
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

    protected static void chainDecase(RightClickBlock event) {
        BlockPos originPos = event.getPos();
        BlockState originState = event.getLevel().getBlockState(originPos);
        Block originBlock = originState.getBlock();

        // 传送带拆壳
        if (originBlock instanceof BeltBlock) {
            event.setCanceled(true);
            decaseBelt(event);
            return;
        }

        if (!(originBlock instanceof RotatedPillarKineticBlock && originBlock instanceof EncasedBlock)) return;
        event.setCanceled(true);

        // 传动方块拆壳
        ((RotatedPillarKineticBlock) originBlock).onSneakWrenched(originState,
                new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec()));

        var axis = originState.getValue(AXIS);
        chain(originPos, axis, pos -> tryDecase(event, pos, axis));
    }

    private static boolean tryDecase(RightClickBlock event, BlockPos p, Axis axis) {
        BlockState s = event.getLevel().getBlockState(p);
        if (s.isAir()) return false;
        Block b = s.getBlock();

        // flag = 轴传动 && 轴同向
        if (!(b instanceof RotatedPillarKineticBlock w) || s.getValue(AXIS) != axis) return false;

        var context = new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec().withPosition(p));
        // 只处理已包机壳的方块
        if (b instanceof EncasedBlock) w.onSneakWrenched(s, context);
        return true;
    }

    private static void decaseBelt(RightClickBlock event) {
        if (event.getEntity().isCrouching()) return;
        event.setCanceled(true);
        BlockPos originPos = event.getPos();
        Level world = event.getLevel();

        BeltBlockEntity beltCtrl = BeltHelper.getControllerBE(world, originPos);
        if (beltCtrl == null) return;

        List<BlockPos> chain = BeltBlock.getBeltChain(world, beltCtrl.getBlockPos());
        if (chain.isEmpty()) return;

        for (BlockPos p : chain) {
            BlockState s = world.getBlockState(p);
            var b = (BeltBlock) s.getBlock();
            b.withBlockEntityDo(world, p, be -> be.setCasingType(BeltBlockEntity.CasingType.NONE));
        }
    }

    private static void chain(BlockPos p, Axis axis, Predicate<BlockPos> tryCasing) {
        S2E ofs = new S2E(p);
        S2E vec = switch (axis) {
            case X -> S2E.axisX();
            case Y -> S2E.axisY();
            case Z -> S2E.axisZ();
        };
        boolean sFlag = true, eFlag = true;
        int count = 1;
        // 沿轴遍历，无所谓传动轴or齿轮
        while ((sFlag || eFlag) && count < MAX_CHAIN) {
            ofs.expand(vec);
            if (sFlag) {
                if (tryCasing.test(ofs.getStart())) count++;
                else sFlag = false;
            }
//            if (count >= MAX_CHAIN) return;
            if (eFlag) {
                if (tryCasing.test(ofs.getEnd())) count++;
                else eFlag = false;
            }
        }// end while
    }
}
