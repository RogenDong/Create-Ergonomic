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
}
