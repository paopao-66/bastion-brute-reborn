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

        for (ServerWorld world : server.getWorlds()) {
            if (!DimensionConfigHelper.isAllowedDimension(world, config.allowedDimensions)) continue;
            if (!config.peacefulModeSpawning && world.getDifficulty().getId() == 0) continue;

            for (ServerPlayerEntity player : world.getPlayers()) {
                // Allow spawning even in creative/spectator for testing
                // Allow spawning even in creative mode
                if (false && (player.isCreative() || player.isSpectator())) continue;
                if (config.requirePlayerInsideStructure
                    && !BastionStructureHelper.isPlayerInsideBastion(player, world)) continue;
                if (exceedsBruteLimit(world, player, config)) continue;

                Optional<BlockPos> bastionPos = BastionStructureHelper.findRandomBastionBlockPos(world, player.getBlockPos());
                if (bastionPos.isEmpty()) continue;

                Optional<BlockPos> spawnPos = BruteSpawnPositionFinder.findSpawnPosition(world, bastionPos.get());
                if (spawnPos.isEmpty()) continue;

                PiglinBruteEntity brute = new PiglinBruteEntity(
                    net.minecraft.entity.EntityType.PIGLIN_BRUTE, world
                );
                brute.setPosition(
                    spawnPos.get().getX() + 0.5,
                    spawnPos.get().getY(),
                    spawnPos.get().getZ() + 0.5
                );
                // Ensure full health initialization
                brute.setHealth(brute.getMaxHealth());
                world.spawnEntity(brute);
                BastionBruteReborn.LOGGER.info("Spawned piglin brute at {} {} {}",
                    spawnPos.get().getX(), spawnPos.get().getY(), spawnPos.get().getZ());
                return;
            }
        }
    }

    private boolean exceedsBruteLimit(ServerWorld world, ServerPlayerEntity player, ModConfig config) {
        Box area = new Box(player.getBlockPos()).expand(config.nearbyPlayerCheckRadius);
        List<PiglinBruteEntity> brutes = world.getEntitiesByClass(
            PiglinBruteEntity.class, area, e -> true
        );
        return brutes.size() >= config.maxBrutesNearPlayer
            || brutes.size() >= config.maxBrutesPerBastion;
    }
}
