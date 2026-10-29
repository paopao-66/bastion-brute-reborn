package com.paopao.bastionbrutereborn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ModConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("bastion_brute_reborn");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bastion_brute_reborn.json");

    public static final ModConfig INSTANCE = load();

    // 掉落设置
    public boolean enableAncientDebrisDrop = true;
    public String dropItem = "minecraft:ancient_debris";
    public double baseChance = 0.025;
    public double lootingBonusPerLevel = 0.01;
    public int maxLootingLevel = 3;
    public int dropAmount = 1;
    public boolean requirePlayerKill = true;
    public boolean allowFakePlayer = true;
    public boolean allowTamedWolfKill = false;
    public boolean respectDoMobLoot = true;

    // 生成设置
    public boolean enablePiglinBruteRespawn = true;
    public int spawnIntervalSeconds = 5;
    public int spawnAmountPerAttempt = 1;
    public int nearbyPlayerCheckRadius = 128;
    public int maxBrutesNearPlayer = 30;
    public List<String> allowedDimensions = Arrays.asList("minecraft:the_nether");
    public boolean requirePlayerInsideStructure = true;
    public boolean peacefulModeSpawning = false;
    public boolean preferBastionBlocks = true;
    public List<String> preferredSpawnBlocks = Arrays.asList(
        "minecraft:blackstone",
        "minecraft:polished_blackstone",
        "minecraft:polished_blackstone_bricks",
        "minecraft:cracked_polished_blackstone_bricks",
        "minecraft:chiseled_polished_blackstone",
        "minecraft:gilded_blackstone"
    );
    public int maxSpawnPositionAttempts = 200;
    public boolean enableDebugLog = false;
    public boolean debugForceSpawnNearPlayer = false;

    private ModConfig() {}

    public static ModConfig load() {
        ModConfig defaults = new ModConfig();
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded == null) {
                    throw new JsonParseException("Configuration must be a JSON object");
                }
                ModConfig config = mergeWithDefaults(loaded, defaults);
                config.sanitize();
                return config;
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Failed to load configuration; using defaults and preserving the existing file", e);
                defaults.sanitize();
                return defaults;
            }
        }
        defaults.sanitize();
        defaults.save();
        return defaults;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (var writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    private void sanitize() {
        spawnIntervalSeconds = Math.max(1, spawnIntervalSeconds);
        nearbyPlayerCheckRadius = Math.max(16, nearbyPlayerCheckRadius);
        maxSpawnPositionAttempts = Math.max(1, maxSpawnPositionAttempts);
        spawnAmountPerAttempt = Math.max(1, spawnAmountPerAttempt);
        dropAmount = Math.max(1, dropAmount);
        maxLootingLevel = Math.max(0, maxLootingLevel);
        baseChance = clampProbability(baseChance);
        lootingBonusPerLevel = clampProbability(lootingBonusPerLevel);
    }

    private static double clampProbability(double value) {
        return Double.isNaN(value) ? 0.0 : Math.max(0.0, Math.min(1.0, value));
    }

    private static ModConfig mergeWithDefaults(ModConfig loaded, ModConfig defaults) {
        if (loaded == null) return defaults;
        if (loaded.allowedDimensions == null) loaded.allowedDimensions = defaults.allowedDimensions;
        if (loaded.preferredSpawnBlocks == null) loaded.preferredSpawnBlocks = defaults.preferredSpawnBlocks;
        return loaded;
    }
}
