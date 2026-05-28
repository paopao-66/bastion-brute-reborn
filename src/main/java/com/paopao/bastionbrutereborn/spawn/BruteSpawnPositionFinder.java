package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;

import java.util.List;
import java.util.Optional;

public final class BruteSpawnPositionFinder {

    private BruteSpawnPositionFinder() {}

    public static Optional<BlockPos> findSpawnPosition(ServerWorld world, BlockPos near) {
        ModConfig config = ModConfig.INSTANCE;

        for (int attempt = 0; attempt < config.maxSpawnPositionAttempts; attempt++) {
            int offsetX = world.random.nextInt(16) - 8;
            int offsetZ = world.random.nextInt(16) - 8;
            BlockPos candidate = near.add(offsetX, 0, offsetZ);

            int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, candidate.getX(), candidate.getZ());
            candidate = new BlockPos(candidate.getX(), surfaceY - 1, candidate.getZ());

            if (isValidSpawnPosition(world, candidate)) {
                if (config.preferBastionBlocks) {
                    BlockState belowState = world.getBlockState(candidate);
                    Identifier blockId = Registries.BLOCK.getId(belowState.getBlock());
                    if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                        return Optional.of(candidate.up());
                    }
                }
                // Fallback: any valid position
                return Optional.of(candidate.up());
            }
        }
        return Optional.empty();
    }

    private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
        BlockPos spawnPos = pos.up();
        BlockState stateBelow = world.getBlockState(pos);
        BlockState stateAt = world.getBlockState(spawnPos);
        BlockState stateAbove = world.getBlockState(spawnPos.up());

        if (stateBelow.isAir()) return false;
        if (!stateAt.isAir()) return false;
        if (!stateAbove.isAir()) return false;

        if (!world.getBlockState(spawnPos).getFluidState().isEmpty()) return false;

        Box box = new Box(spawnPos).expand(0.5);
        if (!world.isSpaceEmpty(box)) return false;

        return true;
    }
}
