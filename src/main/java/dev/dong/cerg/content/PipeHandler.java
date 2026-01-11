package dev.dong.cerg.content;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import dev.dong.cerg.CErg;
import dev.dong.cerg.util.S2E;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static com.simibubi.create.AllBlocks.*;

/**
 * 管道处理
 */
public class PipeHandler {

    static Set<BlockPos> getConnectedPipe(Level world, BlockPos pipePos) {
        LinkedList<BlockPos> frontier = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(pipePos);

        // Visit all connected
        while (!frontier.isEmpty() && visited.size() < CErg.CONFIG.chainEncase.pipeMaxDistance) {
            BlockPos currentPos = frontier.pop();
            if (!world.isLoaded(currentPos) || visited.contains(currentPos)) continue;
            visited.add(currentPos);

            BlockState currentState = world.getBlockState(currentPos);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, currentPos);
            if (pipe == null) continue;

            for (Direction d : FluidPropagator.getPipeConnections(currentState, pipe)) {
                BlockPos target = currentPos.relative(d);
                if (visited.contains(target) || !world.isLoaded(target)) continue;

                BlockState state = world.getBlockState(target);
                if (state.isAir()) continue;
                if (isAxialPipe(state) || ENCASED_FLUID_PIPE.has(state))
                    frontier.add(target);
            }// end for
        }// end while
        return visited;
    }

    public static boolean isAxialPipe(BlockState s) {
        return FLUID_PIPE.has(s) || GLASS_FLUID_PIPE.has(s);
    }

    static Set<BlockPos> getAxialConnectedPipe(Level world, BlockPos p) {
        var os = world.getBlockState(p);
        if (os.isAir()) return null;

        Axis axis = FluidPropagator.getStraightPipeAxis(os);
        if (axis == null) return null;

        S2E ofs = new S2E(p);
        S2E vec = S2E.getVec(axis);
        boolean sFlag = true, eFlag = true;
        HashSet<BlockPos> connected = new HashSet<>();

        // 沿轴遍历，无所谓传动轴or齿轮
        while ((sFlag || eFlag) && connected.size() < CErg.CONFIG.chainEncase.pipeMaxDistance) {
            ofs.expand(vec);
            if (sFlag) {
                var bs = world.getBlockState(ofs.getStart());
                if (isAxialPipe(bs) && FluidPropagator.getStraightPipeAxis(bs) == axis)
                    connected.add(ofs.getStart());
                else sFlag = false;
            }
//            if (count >= CErg.CONFIG.chainEncase.pipeMaxDistance) return;
            if (eFlag) {
                var bs = world.getBlockState(ofs.getEnd());
                if (isAxialPipe(bs) && FluidPropagator.getStraightPipeAxis(bs) == axis)
                    connected.add(ofs.getEnd());
                else eFlag = false;
            }
        }

        return connected;
    }

    /**
     * 连锁切换【常规管道/玻璃管道】
     */
    public static void chainTogglePipe(RightClickBlock event) {
        var level = event.getLevel();
        var originState = level.getBlockState(event.getPos());
        boolean toGlass = FLUID_PIPE.has(originState);
        if (!toGlass && !GLASS_FLUID_PIPE.has(originState)) return;

        Set<BlockPos> connected = getAxialConnectedPipe(level, event.getPos());
        if (connected == null || connected.isEmpty()) return;

        var hand = event.getHand();
        var ray = event.getHitVec();
        var player = event.getEntity();

        for (BlockPos pos : connected) {
            var state = level.getBlockState(pos);
            var block = state.getBlock();
            if (toGlass && block instanceof FluidPipeBlock regularPipe)
                regularPipe.onWrenched(state, new UseOnContext(player, hand, ray.withPosition(pos)));
            else if (!toGlass && block instanceof GlassFluidPipeBlock glassPipe)
                glassPipe.onWrenched(state, new UseOnContext(player, hand, ray.withPosition(pos)));
//            else continue;
        }
    }
}
