package dev.dong.cerg.content;

import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import dev.dong.cerg.util.S2E;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.simibubi.create.AllBlocks.ENCASED_FLUID_PIPE;
import static com.simibubi.create.AllBlocks.GLASS_FLUID_PIPE;
import static com.simibubi.create.AllBlocks.FLUID_PIPE;
import static com.simibubi.create.AllBlocks.ANDESITE_CASING;
import static com.simibubi.create.AllBlocks.COPPER_CASING;
import static com.simibubi.create.AllBlocks.BRASS_CASING;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;
import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.ANDESITE;
import static com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType.BRASS;

public class CasingHandler {

    public static int MAX_CHAIN = 64;

    public static void chainEncase(RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos originPos = event.getPos();
        BlockState originState = level.getBlockState(originPos);

        // 管道
        if ((FLUID_PIPE.has(originState) || GLASS_FLUID_PIPE.has(originState))
                && COPPER_CASING.isIn(event.getItemStack())) {
            encasePipe(event);
            return;
        }

        Block originBlock = originState.getBlock();
        // 传送带
        if (originBlock instanceof BeltBlock) {
            encaseBelt(event);
            return;
        }

        // 传动方块
        if (!(originBlock instanceof RotatedPillarKineticBlock)) return;
        ItemStack heldItemStack = event.getItemStack();
        Axis axis = originState.getValue(AXIS);
        chain(originPos, axis, pos -> tryEncase(event, pos, axis, heldItemStack));
    }

    private static boolean tryEncase(RightClickBlock e, BlockPos pos, Axis a, ItemStack held) {
        Level level = e.getLevel();
        if (!level.isLoaded(pos)) return false;

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

        boolean isBrass = BRASS_CASING.is(heldItem);
        if (!isBrass && !ANDESITE_CASING.is(heldItem)) return;

        SoundType soundType = (isBrass ? BRASS_CASING : ANDESITE_CASING)
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

    private static void encasePipe(RightClickBlock event) {
        var level = event.getLevel();
        Set<BlockPos> connected = getConnectedPipe(level, event.getPos());
        if (connected.isEmpty()) return;

        BlockHitResult hitVec = event.getHitVec();
        var heldItemStack = event.getItemStack();
        var heldItem = heldItemStack.getItem();
        var player = event.getEntity();
        var hand = event.getHand();

        for (BlockPos pos : connected) {
            BlockState state = level.getBlockState(pos);
            Block pipe = state.getBlock();
            BlockHitResult ray = hitVec.withPosition(pos);
            // 玻璃管道
            if (pipe instanceof GlassFluidPipeBlock gp) {
                gp.use(state, level, pos, player, hand, ray);
                continue;
            }
            EncasedBlock e = getEncasedBlock(pipe, heldItem);
            if (e != null) e.handleEncasing(state, level, pos, heldItemStack, player, hand, ray);
        }
    }

    private static Set<BlockPos> getConnectedPipe(Level world, BlockPos pipePos) {
        LinkedList<BlockPos> frontier = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(pipePos);

        // Visit all connected
        while (!frontier.isEmpty() && visited.size() < MAX_CHAIN) {
            BlockPos currentPos = frontier.pop();
            if (!world.isLoaded(currentPos) || visited.contains(currentPos)) continue;

            BlockState currentState = world.getBlockState(currentPos);
            // 跳过已经包壳的
            if (!ENCASED_FLUID_PIPE.has(currentState)) visited.add(currentPos);

            FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, currentPos);
            if (pipe == null) continue;

            for (Direction d : FluidPropagator.getPipeConnections(currentState, pipe)) {
                BlockPos target = currentPos.relative(d);
                if (visited.contains(target) || !world.isLoaded(target)) continue;

                BlockState state = world.getBlockState(target);
                if (!state.isAir() && (FLUID_PIPE.has(state) || GLASS_FLUID_PIPE.has(state)))
                    frontier.add(target);
            }// end for
        }// end while
        return visited;
    }

    public static void chainDecase(RightClickBlock event) {
        BlockPos originPos = event.getPos();
        BlockState originState = event.getLevel().getBlockState(originPos);
        Block originBlock = originState.getBlock();

        // 传送带拆壳
        if (originBlock instanceof BeltBlock) {
            event.setCanceled(true);
            decaseBelt(event);
            return;
        }

        if (!(originBlock instanceof RotatedPillarKineticBlock rpk && originBlock instanceof EncasedBlock)) return;
        event.setCanceled(true);

        // 传动方块拆壳
        rpk.onSneakWrenched(originState, new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec()));

        var axis = originState.getValue(AXIS);
        chain(originPos, axis, pos -> tryDecase(event, pos, axis));
    }

    private static boolean tryDecase(RightClickBlock event, BlockPos p, Axis axis) {
        var level = event.getLevel();
        if (!level.isLoaded(p)) return false;

        BlockState s = level.getBlockState(p);
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
