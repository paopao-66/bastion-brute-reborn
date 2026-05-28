package com.paopao.bastionbrutereborn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bastion_brute_reborn.json");

    public static final ModConfig INSTANCE = load();

    // Drop settings
    public boolean enableAncientDebrisDrop = true;
    public String dropTargetMob = "minecraft:piglin_brute";
    public String dropItem = "minecraft:ancient_debris";
    public double baseChance = 0.025;
    public double lootingBonusPerLevel = 0.01;
    public int maxLootingLevel = 3;
    public int dropAmount = 1;
    public boolean requirePlayerKill = true;
    public boolean allowFakePlayer = true;
    public boolean allowTamedWolfKill = true;
    public boolean respectDoMobLoot = true;
    public boolean showDropMessage = false;

    // Respawn settings
    public boolean enablePiglinBruteRespawn = true;
    public String targetStructure = "minecraft:bastion_remnant";
    public String spawnTargetMob = "minecraft:piglin_brute";
    public int spawnIntervalSeconds = 5;
    public int spawnAmountPerAttempt = 1;
    public int maxBrutesPerBastion = 10;
    public int nearbyPlayerCheckRadius = 128;
    public int maxBrutesNearPlayer = 30;
    public List<String> allowedDimensions = Arrays.asList("minecraft:the_nether");
    public boolean requirePlayerInsideStructure = true;
    public boolean peacefulModeSpawning = false;
    public boolean respectVanillaSpawnRules = true;
    public boolean preferBastionBlocks = true;
    public List<String> preferredSpawnBlocks = Arrays.asList(
        "minecraft:blackstone",
        "minecraft:polished_blackstone",
        "minecraft:polished_blackstone_bricks",
        "minecraft:cracked_polished_blackstone_bricks",
        "minecraft:chiseled_polished_blackstone",
        "minecraft:gilded_blackstone"
    );
    public int maxSpawnPositionAttempts = 10;
    public boolean markSpawnedBrutes = false;
    public boolean showSpawnMessage = false;

    private ModConfig() {}

    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
