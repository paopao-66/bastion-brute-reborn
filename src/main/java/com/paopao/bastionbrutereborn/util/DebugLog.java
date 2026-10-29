package com.paopao.bastionbrutereborn.util;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
import com.paopao.bastionbrutereborn.config.ModConfig;

public final class DebugLog {

    private DebugLog() {}

    public static void log(String format, Object... args) {
        if (ModConfig.INSTANCE.enableDebugLog) {
            BastionBruteReborn.LOGGER.info(format, args);
        }
    }
}
