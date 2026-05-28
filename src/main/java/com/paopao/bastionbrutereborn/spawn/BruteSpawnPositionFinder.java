package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
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

        // Scan in expanding squares around bastion center
        // Step=4 covers every 4 blocks for thorough but fast coverage
        for (int ring = 0; ring <= 8; ring++) {
            int step = 4;
            int limit = ring * step;
            
            // Check every point on the perimeter of this square
            for (int dx = -limit; dx <= limit; dx += (ring == 0 ? 1 : step)) {
                for (int dz = -limit; dz <= limit; dz += (ring == 0 ? 1 : step)) {
                    // For rings > 0, only check perimeter (skip interior)
                    if (ring > 0 && Math.abs(dx) < limit && Math.abs(dz) < limit) continue;

                    int bx = near.getX() + dx;
                    int bz = near.getZ() + dz;

                    // Search from ground-level heights down
                    // Bastion platforms are typically Y=50-80, lava floor Y=31-36
                    for (int y = 85; y > 32; y--) {
                        BlockPos cp = new BlockPos(bx, y, bz);
                        if (isGroundPosition(world, cp)) {
                            if (isValidSpawnPosition(world, cp.down())) {
                                BlockState gs = world.getBlockState(cp.down());
                                if (config.preferBastionBlocks) {
                                    Identifier blockId = Registries.BLOCK.getId(gs.getBlock());
                                    if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                                        return Optional.of(cp);
                                    }
                                }
                                return Optional.of(cp);
                            }
                            break; // found a ground block but not valid, check next column
                        }
                    }
                }
            }
        }

        BastionBruteReborn.LOGGER.info("SpawnFinder: all searches failed");
        return Optional.empty();
    }

    private static boolean isGroundPosition(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos).isAir()
            && !world.getBlockState(pos.down()).isAir()
            && world.getBlockState(pos).getFluidState().isEmpty();
    }

    private static boolean isValidSpawnPosition(ServerWorld world, BlockPos groundBlock) {
        BlockPos spawnPos = groundBlock.up();
        BlockState stateBelow = world.getBlockState(groundBlock);
        BlockState stateAt = world.getBlockState(spawnPos);
        BlockState stateAbove = world.getBlockState(spawnPos.up());

        if (stateBelow.isAir()) return false;
        if (!stateAt.isAir()) return false;
        if (!stateAbove.isAir()) return false;
        if (!world.getBlockState(spawnPos).getFluidState().isEmpty()) return false;

        Box box = new Box(spawnPos).expand(0.4);
        return world.isSpaceEmpty(box);
    }
}
