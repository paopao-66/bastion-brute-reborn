package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;

import java.util.Optional;

public final class BruteSpawnPositionFinder {

    private BruteSpawnPositionFinder() {}

    public static Optional<BlockPos> findSpawnPosition(ServerWorld world, BlockPos near) {
        ModConfig config = ModConfig.INSTANCE;

        for (int attempt = 0; attempt < config.maxSpawnPositionAttempts; attempt++) {
            int offsetX = world.random.nextInt(16) - 8;
            int offsetZ = world.random.nextInt(16) - 8;
            int baseX = near.getX() + offsetX;
            int baseZ = near.getZ() + offsetZ;

            // Find the ground from bottom up instead of top down
            // Start from Y=100 (below ceiling in Nether), search down to Y=20
            for (int y = near.getY() + 30; y > world.getBottomY() + 10; y--) {
                BlockPos checkPos = new BlockPos(baseX, y, baseZ);
                if (isGroundPosition(world, checkPos)) {
                    // checkPos is the air block, below it is the ground block
                    BlockPos groundBlock = checkPos.down();
                    BlockState groundState = world.getBlockState(groundBlock);

                    if (isValidSpawnPosition(world, groundBlock)) {
                        if (config.preferBastionBlocks) {
                            Identifier blockId = Registries.BLOCK.getId(groundState.getBlock());
                            if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                                return Optional.of(checkPos);
                            }
                        }
                        return Optional.of(checkPos);
                    }
                    break;
                }
            }
        }
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
        if (!world.isSpaceEmpty(box)) return false;

        return true;
    }
}
