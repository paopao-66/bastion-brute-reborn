package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Optional;

public final class BruteSpawnPositionFinder {

    private BruteSpawnPositionFinder() {}

    public static Optional<BlockPos> findSpawnPosition(ServerWorld world, BlockPos near) {
        // Scan in fine grid around bastion center (step=2, radius=48)
        // Covers all bastion platforms at any height in the Nether
        for (int dx = -48; dx <= 48; dx += 2) {
            for (int dz = -48; dz <= 48; dz += 2) {
                int bx = near.getX() + dx;
                int bz = near.getZ() + dz;

                for (int y = 120; y > 32; y--) {
                    BlockPos cp = new BlockPos(bx, y, bz);
                    if (isValidSpawnPosition(world, cp)) {
                        return Optional.of(cp);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
        // Check: air at pos, solid non-fluid block below, 2 blocks of air above
        if (!world.getBlockState(pos).isAir()) return false;
        if (!world.getBlockState(pos).getFluidState().isEmpty()) return false;

        BlockPos below = pos.down();
        BlockState stateBelow = world.getBlockState(below);
        if (stateBelow.isAir()) return false;
        if (!stateBelow.getFluidState().isEmpty()) return false; // no lava floor

        // Ensure 2 blocks of space above for the brute
        if (!world.getBlockState(pos.up()).isAir()) return false;
        if (!world.getBlockState(pos.up(2)).isAir()) return false;

        // Check no collision with nearby blocks
        Box box = new Box(pos).expand(0.4);
        return world.isSpaceEmpty(box);
    }
}
