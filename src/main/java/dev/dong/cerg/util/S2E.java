package dev.dong.cerg.util;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

/**
 * start to end
 */
public class S2E {
    // [start, end]
    private final int[] a;

    private S2E(int x, int y, int z) {
        a = new int[]{x, y, z, -x, -y, -z};
    }

    public S2E(BlockPos origin) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();
        a = new int[]{x, y, z, x, y, z};
    }

    public static S2E axisX() {
        return new S2E(1, 0, 0);
    }

    public static S2E axisY() {
        return new S2E(0, 1, 0);
    }

    public static S2E axisZ() {
        return new S2E(0, 0, 1);
    }

    public void expand(S2E vec) {
        for (int i = 0; i < 6; i++)
            this.a[i] += vec.a[i];
    }

    public BlockPos getStart() {
        return new BlockPos(this.a[0], this.a[1], this.a[2]);
    }

    public BlockPos getEnd() {
        return new BlockPos(this.a[3], this.a[4], this.a[5]);
    }

    public Pair<Direction, Direction> getDirection() {
        if (this.a[0] > 0) return Pair.of(Direction.EAST, Direction.WEST);
        if (this.a[1] > 0) return Pair.of(Direction.UP, Direction.DOWN);
        if (this.a[2] > 0) return Pair.of(Direction.SOUTH, Direction.NORTH);
        return null;
    }

    public static S2E getVec(Axis axis) {
        return switch (axis) {
            case X -> S2E.axisX();
            case Y -> S2E.axisY();
            case Z -> S2E.axisZ();
        };
    }

    public static Pair<Direction, Direction> getDirection(Axis axis) {
        return switch (axis) {
            case X -> Pair.of(Direction.EAST, Direction.SOUTH);
            case Y -> Pair.of(Direction.UP, Direction.NORTH);
            case Z -> Pair.of(Direction.SOUTH, Direction.NORTH);
        };
    }
}
