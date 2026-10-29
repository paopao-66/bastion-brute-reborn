package com.paopao.bastionbrutereborn.drop;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
import com.paopao.bastionbrutereborn.config.ModConfig;
import com.paopao.bastionbrutereborn.util.DebugLog;
import com.paopao.bastionbrutereborn.util.EntityKillSourceHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PiglinBruteAncientDebrisDropHandler {

    public static void onEntityDeath(LivingEntity entity, DamageSource source) {
        ModConfig config = ModConfig.INSTANCE;

        World world = entity.getEntityWorld();
        DebugLog.log("DropHandler: entity={} world={}", entity, world);

        if (!config.enableAncientDebrisDrop) {
            DebugLog.log("DropHandler: disabled by config");
            return;
        }
        if (!(entity instanceof PiglinBruteEntity)) {
            DebugLog.log("DropHandler: not a piglin brute: {}", entity.getType());
            return;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            DebugLog.log("DropHandler: not server world");
            return;
        }

        if (config.respectDoMobLoot) {
            if (!serverWorld.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_MOB_LOOT)) {
                DebugLog.log("DropHandler: doMobLoot is false");
                return;
            }
        }

        // source is provided by the event directly
        DebugLog.log("DropHandler: source={}", source);

        if (source == null) {
            BastionBruteReborn.LOGGER.warn("DropHandler: source is null, skipping drop");
            return;
        }

        if (!isValidKillSource(entity, source, config)) {
            DebugLog.log("DropHandler: invalid kill source");
            return;
        }

        int lootingLevel = getLootingLevel(source, serverWorld);
        int effectiveLooting = Math.min(lootingLevel, config.maxLootingLevel);
        double chance = config.baseChance + effectiveLooting * config.lootingBonusPerLevel;

        DebugLog.log("DropHandler: looting={} effective={} chance={}", lootingLevel, effectiveLooting, chance);

        if (entity.getRandom().nextDouble() < chance) {
            ItemStack stack = new ItemStack(resolveDropItem(config.dropItem));
            stack.setCount(Math.min(config.dropAmount, stack.getMaxCount()));
            ItemEntity itemEntity = new ItemEntity(
                serverWorld,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                stack
            );
            itemEntity.setToDefaultPickupDelay();
            serverWorld.spawnEntity(itemEntity);
            DebugLog.log("DropHandler: DROPPED {} {} at {},{}", config.dropAmount, config.dropItem, entity.getX(), entity.getZ());
        } else {
            DebugLog.log("DropHandler: roll failed (chance={})", chance);
        }
    }

    private static Item resolveDropItem(String configuredItem) {
        Identifier id = configuredItem == null ? null : Identifier.tryParse(configuredItem);
        if (id != null && Registries.ITEM.containsId(id)) {
            Item item = Registries.ITEM.get(id);
            if (item != Items.AIR) return item;
        }
        BastionBruteReborn.LOGGER.warn("Invalid dropItem '{}'; falling back to minecraft:ancient_debris", configuredItem);
        return Items.ANCIENT_DEBRIS;
    }

    private static boolean isValidKillSource(LivingEntity entity, DamageSource source, ModConfig config) {
        if (config.requirePlayerKill) {
            if (!EntityKillSourceHelper.isPlayerKill(entity, source)) {
                return config.allowTamedWolfKill && EntityKillSourceHelper.isTamedWolfKill(source);
            }
            if (source.getAttacker() instanceof PlayerEntity player) {
                if (!config.allowFakePlayer && EntityKillSourceHelper.isFakePlayer(player)) return false;
            }
            return true;
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
