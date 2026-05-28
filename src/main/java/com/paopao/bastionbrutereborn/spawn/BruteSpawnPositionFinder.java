package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Optional;

public final class BruteSpawnPositionFinder {

    private BruteSpawnPositionFinder() {}

    public static Optional<BlockPos> findSpawnPosition(ServerWorld world, BlockPos near) {
        ModConfig config = ModConfig.INSTANCE;

        // Random sampling within bastion radius for performance and variety
        for (int attempt = 0; attempt < 200; attempt++) {
            int dx = world.random.nextInt(80) - 40;
            int dz = world.random.nextInt(80) - 40;
            int bx = near.getX() + dx;
            int bz = near.getZ() + dz;

            for (int y = 120; y > 32; y--) {
                BlockPos cp = new BlockPos(bx, y, bz);
                if (isValidSpawnPosition(world, cp)) {
                    // Prefer bastion blocks if configured
                    if (config.preferBastionBlocks) {
                        Identifier blockId = Registries.BLOCK.getId(
                            world.getBlockState(cp.down()).getBlock());
                        if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                            return Optional.of(cp);
                        }
                        continue; // found valid spot but not preferred block, keep looking
                    }
                    return Optional.of(cp);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
        // Feet position: must be air, no fluid
        if (!world.getBlockState(pos).isAir()) return false;
        if (!world.getBlockState(pos).getFluidState().isEmpty()) return false;

        // Ground below: solid, no fluid
        BlockPos below = pos.down();
        BlockState stateBelow = world.getBlockState(below);
        if (stateBelow.isAir()) return false;
        if (!stateBelow.getFluidState().isEmpty()) return false;

        // Body space: brute is 1.95 blocks tall
        if (!world.getBlockState(pos.up()).isAir()) return false;
        // Head space: allow transparent blocks (not just strict air)
        if (world.getBlockState(pos.up(2)).isSolidBlock(world, pos.up(2))) return false;

        // No collision with surroundings
        Box box = new Box(pos).expand(0.4);
        return world.isSpaceEmpty(box);
    }
}
