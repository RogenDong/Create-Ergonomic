package dev.dong.cerg.util;

import net.minecraft.core.BlockPos;

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
}
