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
        BlockPos fallback = null;

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
                        // Save first valid spot as fallback
                        if (fallback == null) {
                            fallback = cp;
                        }
                    } else {
                        return Optional.of(cp);
                    }
                    break; // found ground in this column, move to next attempt
                }
            }
        }

        // Return any valid spot if preferred blocks not found
        if (fallback != null) {
            return Optional.of(fallback);
        }

        return Optional.empty();
    }

    private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
        if (!world.getBlockState(pos).isAir()) return false;
        if (!world.getBlockState(pos).getFluidState().isEmpty()) return false;

        BlockPos below = pos.down();
        BlockState stateBelow = world.getBlockState(below);
        if (stateBelow.isAir()) return false;
        if (!stateBelow.getFluidState().isEmpty()) return false;

        if (!world.getBlockState(pos.up()).isAir()) return false;
        if (world.getBlockState(pos.up(2)).isSolidBlock(world, pos.up(2))) return false;

        Box box = new Box(pos).expand(0.4);
        return world.isSpaceEmpty(box);
    }
}
