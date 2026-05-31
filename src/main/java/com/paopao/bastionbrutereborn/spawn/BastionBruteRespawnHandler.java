package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
import com.paopao.bastionbrutereborn.config.ModConfig;
import com.paopao.bastionbrutereborn.util.DimensionConfigHelper;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Optional;

public class BastionBruteRespawnHandler {

    private int tickCounter = 0;

    public void tick(MinecraftServer server) {
        ModConfig config = ModConfig.INSTANCE;
        if (!config.enablePiglinBruteRespawn) return;

        tickCounter++;
        int intervalTicks = config.spawnIntervalSeconds * 20;
        if (tickCounter < intervalTicks) return;
        tickCounter = 0;

        log("[BBR] Tick spawn check started. interval={}s dedicatedServer={}", config.spawnIntervalSeconds, server.isDedicated());

        for (ServerWorld world : server.getWorlds()) {
            log("[BBR] Checking world: {}", world.getRegistryKey().getValue());

            // 维度检查
            boolean dimensionAllowed = DimensionConfigHelper.isAllowedDimension(world, config.allowedDimensions);
            log("[BBR] Dimension allowed: {}", dimensionAllowed);
            if (!dimensionAllowed) continue;

            // 和平模式检查
            boolean peaceful = !config.peacefulModeSpawning && world.getDifficulty().getId() == 0;
            log("[BBR] Peaceful blocked: {}", peaceful);
            if (peaceful) continue;

            List<ServerPlayerEntity> players = world.getPlayers();
            if (players.isEmpty()) {
                log("[BBR] 跳过世界：当前维度没有玩家");
                continue;
            }

            for (ServerPlayerEntity player : players) {
                BlockPos pPos = player.getBlockPos();
                log("[BBR] Checking player: {} at {} {} {}", 
                    player.getNameForScoreboard(), pPos.getX(), pPos.getY(), pPos.getZ());

                // ---- 调试强制生成模式 ----
                if (config.debugForceSpawnNearPlayer) {
                    log("[BBR] debugForceSpawnNearPlayer=true, 跳过所有结构判断");
                    trySpawn(player, world, config, Optional.empty());
                    continue;
                }

                // ---- 普通模式：结构判断 ----
                boolean bastionNearby = BastionStructureHelper.isBastionNearPlayer(player, world);
                log("[BBR] Bastion nearby: {}", bastionNearby);

                if (config.requirePlayerInsideStructure && !bastionNearby) {
                    log("[BBR] 跳过生成：玩家附近无堡垒结构引用");
                    continue;
                }

                // 蛮兵数量检查
                if (exceedsBruteLimit(world, player, config)) {
                    log("[BBR] 跳过生成：已达蛮兵数量上限");
                    continue;
                }

                // 查找候选中心
                Optional<BlockPos> candidate = BastionStructureHelper.findCandidateBastionCenter(world, pPos);
                log("[BBR] Candidate center: {}", candidate.map(p -> p.getX() + " " + p.getY() + " " + p.getZ()).orElse("none"));

                // 执行生成
                trySpawn(player, world, config, candidate);
            }
        }
    }

    /**
     * 尝试为指定玩家生成蛮兵。
     * 使用三层搜索逻辑查找生成位置。
     */
    private void trySpawn(ServerPlayerEntity player, ServerWorld world, ModConfig config, Optional<BlockPos> candidate) {
        // 生成本身也要检查数量（debugForceSpawnNearPlayer 模式跳过结构判断但不跳过数量限制）
        if (exceedsBruteLimit(world, player, config)) {
            log("[BBR] 跳过生成（trySpawn内）：已达蛮兵数量上限");
            return;
        }

        // 执行三层搜索
        Optional<BlockPos> spawnPos = BruteSpawnPositionFinder.findSpawnPosition(world, player, candidate);
        log("[BBR] Spawn position found: {}", spawnPos.map(p -> p.getX() + " " + p.getY() + " " + p.getZ()).orElse("none"));

        if (spawnPos.isEmpty()) {
            log("[BBR] Spawn failed: no valid position");
            return;
        }

        BlockPos pos = spawnPos.get();
        PiglinBruteEntity brute = new PiglinBruteEntity(
            net.minecraft.entity.EntityType.PIGLIN_BRUTE, world
        );
        brute.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        brute.setHealth(brute.getMaxHealth());

        boolean spawned = world.spawnEntity(brute);
        if (spawned) {
            log("[BBR] Spawn success: spawned piglin brute at {} {} {}", pos.getX(), pos.getY(), pos.getZ());
        } else {
            log("[BBR] Spawn failed: world.spawnEntity returned false at {} {} {}", pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /**
     * 检查玩家附近蛮兵数量是否已达上限。
     * 仅使用 maxBrutesNearPlayer 判断。
     */
    private boolean exceedsBruteLimit(ServerWorld world, ServerPlayerEntity player, ModConfig config) {
        Box area = new Box(player.getBlockPos()).expand(config.nearbyPlayerCheckRadius);
        List<PiglinBruteEntity> brutes = world.getEntitiesByClass(
            PiglinBruteEntity.class, area, e -> true
        );
        boolean limitReached = brutes.size() >= config.maxBrutesNearPlayer;
        log("[BBR] Brute count near player: {}/{}", brutes.size(), config.maxBrutesNearPlayer);
        return limitReached;
    }

    private void log(String format, Object... args) {
        if (ModConfig.INSTANCE.enableDebugLog) {
            BastionBruteReborn.LOGGER.info(format, args);
        }
    }
}
