package dev.dong.cerg.content;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import dev.dong.cerg.CErg;
import dev.dong.cerg.CErgConfig;
import dev.dong.cerg.util.S2E;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

import java.util.*;

import static com.simibubi.create.AllBlocks.FLUID_PIPE;
import static com.simibubi.create.AllBlocks.GLASS_FLUID_PIPE;
import static com.simibubi.create.AllBlocks.ENCASED_FLUID_PIPE;
import static net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION;

/**
 * 管道处理
 */
public class PipeHandler {

    private static final CErgConfig.FiniteChain cfg = CErg.CONFIG.finiteChain;

    public static Set<BlockPos> getConnectedPipe(Level world, BlockPos pipePos) {
        LinkedList<BlockPos> frontier = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>(Math.max(64, cfg.pipeMaxDistance));
        frontier.add(pipePos);

        // Visit all connected
        while (!frontier.isEmpty() && visited.size() < cfg.pipeMaxDistance) {
            BlockPos currentPos = frontier.pop();
            if (!world.isLoaded(currentPos) || visited.contains(currentPos)) continue;
            visited.add(currentPos);

            BlockState currentState = world.getBlockState(currentPos);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, currentPos);
            if (pipe == null) continue;

            for (Direction d : FluidPropagator.getPipeConnections(currentState, pipe)) {
                BlockPos target = currentPos.relative(d);
                if (visited.contains(target) || !world.isLoaded(target)) continue;

                var s = world.getBlockState(target);
                if (FLUID_PIPE.has(s) || GLASS_FLUID_PIPE.has(s) || ENCASED_FLUID_PIPE.has(s))
                    frontier.add(target);
            }// end for
        }// end while
        return visited;
    }

    public static boolean isAxialPipe(BlockState s) {
        return GLASS_FLUID_PIPE.has(s) || (
                FLUID_PIPE.has(s) && FluidPropagator.getStraightPipeAxis(s) != null);
    }

    private static boolean testConnection(Level world, Axis axis, List<BlockPos> axialPipes, Direction dir, BlockPos pos) {
        var bs = world.getBlockState(pos);
        FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, pos);
        if (pipe == null) return false;

        if (!ENCASED_FLUID_PIPE.has(bs) && FluidPropagator.getStraightPipeAxis(bs) == axis) {
            axialPipes.add(pos);
            return true;
        }

        // 遇到[非轴向管道 or 套壳管道]: 若沿指定方向连接, 则跳过节点继续遍历; 反之则停止
        return pipe.canHaveFlowToward(bs, dir);
    }

    public static List<BlockPos> getAxialConnectedPipe(Level world, BlockPos p) {
        var os = world.getBlockState(p);
        if (os.isAir()) return null;

        var axis = FluidPropagator.getStraightPipeAxis(os);
        if (axis == null) return null;

        S2E ofs = new S2E(p);
        S2E vec = S2E.getVec(axis);
        var dir = vec.getDirection();
        boolean sFlag = true, eFlag = true;
        List<BlockPos> axialPipes = new ArrayList<>(Math.max(64, cfg.axialDistance));

        // 遍历直通管道（因为沿轴向遍历，所以套用轴向连锁限制）
        while ((sFlag || eFlag) && axialPipes.size() < cfg.axialDistance) {
            ofs.expand(vec);
            if (sFlag) sFlag = testConnection(world, axis, axialPipes, dir.getFirst(), ofs.getStart());
            if (axialPipes.size() >= cfg.axialDistance) break;
            if (eFlag) eFlag = testConnection(world, axis, axialPipes, dir.getSecond(), ofs.getEnd());
        }

        return axialPipes;
    }

    /**
     * 连锁切换【常规管道/玻璃管道】
     */
    public static void chainTogglePipe(RightClickBlock event) {
        var level = event.getLevel();
        var originPos = event.getPos();
        var originState = level.getBlockState(originPos);
        boolean toGlass = FLUID_PIPE.has(originState);
        if (toGlass && FluidPropagator.getStraightPipeAxis(originState) == null) return;
//        if (!toGlass && !GLASS_FLUID_PIPE.has(originState)) return;

        List<BlockPos> connected = getAxialConnectedPipe(level, originPos);
        if (connected == null || connected.isEmpty()) return;

        var hand = event.getHand();
        var ray = event.getHitVec();
        var player = event.getEntity();

        for (BlockPos pos : connected) {
            var state = level.getBlockState(pos);
            if (toGlass && state.getBlock() instanceof FluidPipeBlock regularPipe)
                regularPipe.onWrenched(state, new UseOnContext(player, hand, ray.withPosition(pos)));
            else if (!toGlass && state.getBlock() instanceof GlassFluidPipeBlock glassPipe)
                glassPipe.onWrenched(state, new UseOnContext(player, hand, ray.withPosition(pos)));
//            else continue;
        }
    }

    public static void togglePipeConnection(RightClickBlock event) {
        var level = event.getLevel();
        var player = event.getEntity();

        var clickFace = event.getFace();
        if (clickFace == null) return;

        var originPos = event.getPos();
        var originState = level.getBlockState(originPos);
        var targetFace = player.isShiftKeyDown() ? clickFace.getOpposite() : clickFace;
        var pre = originState.getValue(PROPERTY_BY_DIRECTION.get(targetFace));

        // 手部动画
        player.swing(event.getHand(), true);

        // 最少保留2个面
        long countOpenFace = Direction.stream()
                .filter(f -> originState.getValue(PROPERTY_BY_DIRECTION.get(f)))
                .count();
        if (countOpenFace < 3 && pre) return;

        // 音效
        var soundType = originState.getSoundType(level, originPos, player);
        level.playSound(null, originPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        level.setBlockAndUpdate(originPos,
                originState.setValue(PROPERTY_BY_DIRECTION.get(targetFace), !pre));
    }
}
