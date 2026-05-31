package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
import com.paopao.bastionbrutereborn.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Optional;

public final class BruteSpawnPositionFinder {

    private BruteSpawnPositionFinder() {}

    /**
     * 第一层：玩家附近搜索。
     * 以玩家为中心，在半径内随机搜索合法生成位置。
     * Y 范围围绕玩家 Y 上下浮动。
     */
    public static Optional<BlockPos> findSpawnNearPlayer(ServerWorld world, PlayerEntity player) {
        ModConfig config = ModConfig.INSTANCE;
        int radius = Math.min(config.nearbyPlayerCheckRadius, 64);
        int maxAttempts = config.maxSpawnPositionAttempts;
        BlockPos playerPos = player.getBlockPos();
        int playerY = playerPos.getY();

        int worldBottomY = world.getBottomY();
        int searchTop = Math.min(playerY + 24, 320);
        int searchBottom = Math.max(playerY - 32, worldBottomY + 1);

        BastionBruteReborn.LOGGER.info(
            "玩家附近搜索：中心({}, {}, {}) 半径{} Y范围{}->{} 尝试{}次",
            playerPos.getX(), playerY, playerPos.getZ(),
            radius, searchBottom, searchTop, maxAttempts);

        BlockPos fallback = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int dx = world.random.nextInt(radius * 2) - radius;
            int dz = world.random.nextInt(radius * 2) - radius;
            int bx = playerPos.getX() + dx;
            int bz = playerPos.getZ() + dz;

            // 不要太近（避免贴脸）
            double distSq = (bx - playerPos.getX()) * (bx - playerPos.getX())
                          + (bz - playerPos.getZ()) * (bz - playerPos.getZ());
            if (distSq < 64) continue;

            // 从高到低扫描 Y
            for (int y = searchTop; y >= searchBottom; y--) {
                BlockPos cp = new BlockPos(bx, y, bz);
                if (!isValidSpawnPosition(world, cp)) continue;

                if (config.preferBastionBlocks) {
                    Identifier blockId = Registries.BLOCK.getId(
                        world.getBlockState(cp.down()).getBlock());
                    if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                        BastionBruteReborn.LOGGER.info("玩家附近找到偏好方块位置：{}", cp);
                        return Optional.of(cp);
                    }
                    if (fallback == null) {
                        fallback = cp;
                    }
                } else {
                    return Optional.of(cp);
                }
                break;
            }
        }

        if (fallback != null) {
            BastionBruteReborn.LOGGER.info("玩家附近使用回退位置（非偏好方块）：{}", fallback);
        } else {
            BastionBruteReborn.LOGGER.info("玩家附近搜索未找到有效位置");
        }
        return fallback != null ? Optional.of(fallback) : Optional.empty();
    }

    /**
     * 第二层：Bastion 候选中心附近搜索。
     */
    public static Optional<BlockPos> findSpawnNearCandidate(ServerWorld world, BlockPos candidate) {
        ModConfig config = ModConfig.INSTANCE;
        int maxAttempts = config.maxSpawnPositionAttempts;
        int radius = 48;
        int candidateY = candidate.getY();
        int worldBottomY = world.getBottomY();
        int searchTop = Math.min(candidateY + 24, 320);
        int searchBottom = Math.max(candidateY - 32, worldBottomY + 1);

        BastionBruteReborn.LOGGER.info(
            "候选中心附近搜索：中心({}, {}, {}) Y范围{}->{}",
            candidate.getX(), candidateY, candidate.getZ(),
            searchBottom, searchTop);

        BlockPos fallback = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int dx = world.random.nextInt(radius * 2) - radius;
            int dz = world.random.nextInt(radius * 2) - radius;
            int bx = candidate.getX() + dx;
            int bz = candidate.getZ() + dz;

            for (int y = searchTop; y >= searchBottom; y--) {
                BlockPos cp = new BlockPos(bx, y, bz);
                if (!isValidSpawnPosition(world, cp)) continue;

                if (config.preferBastionBlocks) {
                    Identifier blockId = Registries.BLOCK.getId(
                        world.getBlockState(cp.down()).getBlock());
                    if (config.preferredSpawnBlocks.contains(blockId.toString())) {
                        BastionBruteReborn.LOGGER.info("候选中心附近找到偏好方块位置：{}", cp);
                        return Optional.of(cp);
                    }
                    if (fallback == null) {
                        fallback = cp;
                    }
                } else {
                    return Optional.of(cp);
                }
                break;
            }
        }

        if (fallback != null) {
            BastionBruteReborn.LOGGER.info("候选中心附近使用回退位置：{}", fallback);
        }
        return fallback != null ? Optional.of(fallback) : Optional.empty();
    }

    /**
     * 第三层：宽松 Fallback。
     */
    public static Optional<BlockPos> findSpawnFallback(ServerWorld world, BlockPos near) {
        ModConfig config = ModConfig.INSTANCE;
        int maxAttempts = config.maxSpawnPositionAttempts;
        int worldBottomY = world.getBottomY();
        BastionBruteReborn.LOGGER.info("进入宽松 Fallback 搜索，近点：{}", near);

        for (int attempt = 0; attempt < maxAttempts * 2; attempt++) {
            int dx = world.random.nextInt(80) - 40;
            int dz = world.random.nextInt(80) - 40;
            int bx = near.getX() + dx;
            int bz = near.getZ() + dz;

            for (int y = 300; y > worldBottomY + 1; y -= 1) {
                BlockPos cp = new BlockPos(bx, y, bz);
                if (isValidSpawnPosition(world, cp)) {
                    BastionBruteReborn.LOGGER.info("Fallback 找到位置：{}", cp);
                    return Optional.of(cp);
                }
            }
        }
        BastionBruteReborn.LOGGER.info("Fallback 搜索也未找到任何有效位置");
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
