package com.paopao.bastionbrutereborn.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class EntityKillSourceHelper {

    private EntityKillSourceHelper() {}

    public static boolean isPlayerKill(LivingEntity entity, DamageSource source) {
        if (source == null || entity == null) return false;

        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity) {
            return true;
        }
        if (attacker instanceof ProjectileEntity projectile) {
            return projectile.getOwner() instanceof PlayerEntity;
        }
        return false;
    }

    public static boolean isFakePlayer(PlayerEntity player) {
        if (player == null) return false;
        if (player instanceof ServerPlayerEntity sp) {
            return true; // fake player check - server side
        }
        return false;
    }

    public static boolean isTamedWolfKill(DamageSource source) {
        if (source == null) return false;
        if (source.getAttacker() instanceof WolfEntity wolf) {
            return wolf.isTamed();
        }
        return false;
    }
}
