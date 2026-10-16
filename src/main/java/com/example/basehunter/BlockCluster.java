package com.example.basehunter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public class BlockCluster {
    public final Vec3d centroid;
    public final int count;
    public final BlockPos representativePos;

    public BlockCluster(List<BlockPos> blocks) {
        this.count = blocks.size();
        this.representativePos = blocks.get(0);

        double sumX = 0, sumY = 0, sumZ = 0;
        for (BlockPos pos : blocks) {
            sumX += pos.getX() + 0.5;
            sumY += pos.getY() + 0.5;
            sumZ += pos.getZ() + 0.5;
        }
        this.centroid = new Vec3d(sumX / count, sumY / count, sumZ / count);
    }
}