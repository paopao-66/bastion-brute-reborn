package com.paopao.bastionbrutereborn.drop;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
import com.paopao.bastionbrutereborn.config.ModConfig;
import com.paopao.bastionbrutereborn.util.EntityKillSourceHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PiglinBruteAncientDebrisDropHandler {

    public static void onEntityDeath(LivingEntity entity, DamageSource source) {
        ModConfig config = ModConfig.INSTANCE;

        World w = entity.getEntityWorld(); BastionBruteReborn.LOGGER.info("DropHandler: entity={} world={}", entity, w);

        if (!config.enableAncientDebrisDrop) {
            BastionBruteReborn.LOGGER.info("DropHandler: disabled by config");
            return;
        }
        if (!(entity instanceof PiglinBruteEntity)) {
            BastionBruteReborn.LOGGER.info("DropHandler: not a piglin brute: {}", entity.getType());
            return;
        }
        World world = entity.getEntityWorld(); if (!(world instanceof ServerWorld serverWorld)) {
            BastionBruteReborn.LOGGER.info("DropHandler: not server world");
            return;
        }

        if (config.respectDoMobLoot) {
            if (!serverWorld.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_MOB_LOOT)) {
                BastionBruteReborn.LOGGER.info("DropHandler: doMobLoot is false");
                return;
            }
        }

        // source is provided by the event directly
        BastionBruteReborn.LOGGER.info("DropHandler: source={}", source);

        if (source == null) {
            BastionBruteReborn.LOGGER.warn("DropHandler: source is null, skipping drop");
            return;
        }

        if (!isValidKillSource(entity, source, config)) {
            BastionBruteReborn.LOGGER.info("DropHandler: invalid kill source");
            return;
        }

        int lootingLevel = getLootingLevel(source, serverWorld);
        int effectiveLooting = Math.min(lootingLevel, config.maxLootingLevel);
        double chance = config.baseChance + effectiveLooting * config.lootingBonusPerLevel;

        BastionBruteReborn.LOGGER.info("DropHandler: looting={} effective={} chance={}", lootingLevel, effectiveLooting, chance);

        if (entity.getRandom().nextDouble() < chance) {
            ItemStack stack = new ItemStack(Items.ANCIENT_DEBRIS, config.dropAmount);
            ItemEntity itemEntity = new ItemEntity(
                serverWorld,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                stack
            );
            itemEntity.setToDefaultPickupDelay();
            serverWorld.spawnEntity(itemEntity);
            BastionBruteReborn.LOGGER.info("DropHandler: DROPPED ancient debris at {},{}", entity.getX(), entity.getZ());
        } else {
            BastionBruteReborn.LOGGER.info("DropHandler: roll failed (chance={})", chance);
        }
    }

    private static boolean isValidKillSource(LivingEntity entity, DamageSource source, ModConfig config) {
        if (config.requirePlayerKill) {
            if (source.getAttacker() instanceof PlayerEntity player) {
                if (!config.allowFakePlayer && EntityKillSourceHelper.isFakePlayer(player)) return false;
                return true;
            }
            if (config.allowTamedWolfKill && EntityKillSourceHelper.isTamedWolfKill(source)) return true;
            return false;
        }
        return true;
    }

    private static int getLootingLevel(DamageSource source, ServerWorld world) {
        if (source.getAttacker() instanceof LivingEntity living) {
            ItemStack weapon = living.getMainHandStack();
            if (!weapon.isEmpty()) {
                var lootingEntry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT)
                    .getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.ofVanilla("looting")));
                return lootingEntry.map(entry -> EnchantmentHelper.getLevel(entry, weapon)).orElse(0);
            }
        }
        return 0;
    }
}
