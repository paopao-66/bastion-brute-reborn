package com.paopao.bastionbrutereborn;

import com.paopao.bastionbrutereborn.config.ModConfig;
import com.paopao.bastionbrutereborn.drop.PiglinBruteAncientDebrisDropHandler;
import com.paopao.bastionbrutereborn.spawn.BastionBruteRespawnHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BastionBruteReborn implements ModInitializer {

    public static final String MOD_ID = "bastion_brute_reborn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final BastionBruteRespawnHandler respawnHandler = new BastionBruteRespawnHandler();

    @Override
    public void onInitialize() {
        LOGGER.info("正在初始化 Bastion Brute Reborn...");

        ModConfig.INSTANCE.save();

        ServerLivingEntityEvents.AFTER_DEATH.register(
            (LivingEntity entity, DamageSource source) -> {
                PiglinBruteAncientDebrisDropHandler.onEntityDeath(entity, source);
            }
        );

        ServerTickEvents.END_SERVER_TICK.register(
            server -> respawnHandler.tick(server)
        );

        LOGGER.info("生成管理器已注册。");
        LOGGER.info("Bastion Brute Reborn 初始化完成。");
    }
}
