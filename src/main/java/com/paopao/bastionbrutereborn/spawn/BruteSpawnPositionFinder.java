package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.config.ModConfig;
import com.paopao.bastionbrutereborn.util.DebugLog;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.Optional;

public final class BruteSpawnPositionFinder {

    private BruteSpawnPositionFinder() {}

    /** 玩家附近搜索，保留至少 8 格的水平距离。 */
    public static Optional<BlockPos> findSpawnNearPlayer(ServerWorld world, PlayerEntity player) {
        int radius = Math.min(ModConfig.INSTANCE.nearbyPlayerCheckRadius, 64);
        return searchAround(world, player.getBlockPos(), radius, 64, "玩家附近");
    }

    /** Bastion 候选中心附近搜索。 */
    public static Optional<BlockPos> findSpawnNearCandidate(ServerWorld world, BlockPos candidate) {
        return searchAround(world, candidate, 48, 0, "候选中心附近");
    }

    private static Optional<BlockPos> searchAround(ServerWorld world, BlockPos center, int radius, int minDistSq, String label) {
        ModConfig config = ModConfig.INSTANCE;
        radius = Math.max(1, radius);
        int logicalTop = world.getBottomY() + world.getLogicalHeight() - 1;
        int searchTop = Math.min(center.getY() + 24, logicalTop);
        int searchBottom = Math.max(center.getY() - 32, world.getBottomY() + 1);
        DebugLog.log("{}搜索：中心({}, {}, {}) 半径{} Y范围{}->{} 尝试{}次",
            label, center.getX(), center.getY(), center.getZ(), radius,
            searchBottom, searchTop, config.maxSpawnPositionAttempts);

        BlockPos fallback = null;
        for (int attempt = 0; attempt < config.maxSpawnPositionAttempts; attempt++) {
            int dx = world.random.nextInt(radius * 2) - radius;
            int dz = world.random.nextInt(radius * 2) - radius;
            if (dx * dx + dz * dz < minDistSq) continue;
            int bx = center.getX() + dx;
            int bz = center.getZ() + dz;
            if (!world.isChunkLoaded(ChunkSectionPos.getSectionCoord(bx), ChunkSectionPos.getSectionCoord(bz))) continue;

            for (int y = searchTop; y >= searchBottom; y--) {
                BlockPos cp = new BlockPos(bx, y, bz);
                if (!isValidSpawnPosition(world, cp)) continue;
                if (!config.preferBastionBlocks) return Optional.of(cp);

                Identifier blockId = Registries.BLOCK.getId(world.getBlockState(cp.down()).getBlock());
                if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                    DebugLog.log("{}找到偏好方块位置：{}", label, cp);
                    return Optional.of(cp);
                }
                if (fallback == null) fallback = cp;
                break;
            }
        }

        if (fallback != null) {
            DebugLog.log("{}使用回退位置（非偏好方块）：{}", label, fallback);
        } else {
            DebugLog.log("{}搜索未找到有效位置", label);
        }
        return Optional.ofNullable(fallback);
    }

    /**
     * 第三层：宽松 Fallback。
     */
    public static Optional<BlockPos> findSpawnFallback(ServerWorld world, BlockPos near) {
        ModConfig config = ModConfig.INSTANCE;
        int maxAttempts = config.maxSpawnPositionAttempts;
        int worldBottomY = world.getBottomY();
        int logicalTop = worldBottomY + world.getLogicalHeight() - 1;
        DebugLog.log("进入宽松 Fallback 搜索，近点：{}", near);

        for (long attempt = 0; attempt < maxAttempts * 2L; attempt++) {
            int dx = world.random.nextInt(80) - 40;
            int dz = world.random.nextInt(80) - 40;
            int bx = near.getX() + dx;
            int bz = near.getZ() + dz;

            if (!world.isChunkLoaded(ChunkSectionPos.getSectionCoord(bx), ChunkSectionPos.getSectionCoord(bz))) continue;

            for (int y = logicalTop; y > worldBottomY + 1; y -= 1) {
                BlockPos cp = new BlockPos(bx, y, bz);
                if (isValidSpawnPosition(world, cp)) {
                    DebugLog.log("Fallback 找到位置：{}", cp);
                    return Optional.of(cp);
                }
            }
        }
        DebugLog.log("Fallback 搜索也未找到任何有效位置");
        return Optional.empty();
    }

    /**
     * 完整三层搜索入口。
     */
    public static Optional<BlockPos> findSpawnPosition(ServerWorld world, PlayerEntity player, Optional<BlockPos> candidateCenter) {
        Optional<BlockPos> pos = findSpawnNearPlayer(world, player);
        if (pos.isPresent()) return pos;

        if (candidateCenter.isPresent()) {
            pos = findSpawnNearCandidate(world, candidateCenter.get());
            if (pos.isPresent()) return pos;
        }

        BlockPos near = candidateCenter.orElse(player.getBlockPos());
        return findSpawnFallback(world, near);
    }

    private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
        int logicalTop = world.getBottomY() + world.getLogicalHeight() - 1;
        if (pos.getY() + 2 > logicalTop) return false;
        if (!world.getBlockState(pos).isAir()) return false;
        if (!world.getBlockState(pos).getFluidState().isEmpty()) return false;

        BlockPos below = pos.down();
        BlockState stateBelow = world.getBlockState(below);
        if (stateBelow.isAir()) return false;
        if (!stateBelow.getFluidState().isEmpty()) return false;

        if (!world.getBlockState(pos.up()).isAir()) return false;
        if (world.getBlockState(pos.up(2)).isSolidBlock(world, pos.up(2))) return false;

        Box box = new Box(
            pos.getX() + 0.2, pos.getY(), pos.getZ() + 0.2,
            pos.getX() + 0.8, pos.getY() + 1.95, pos.getZ() + 0.8
        );
        return world.isSpaceEmpty(box);
    }
}
