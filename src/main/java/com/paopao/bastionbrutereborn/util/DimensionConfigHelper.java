package com.paopao.bastionbrutereborn.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.List;

public final class DimensionConfigHelper {

    private DimensionConfigHelper() {}

    public static boolean isAllowedDimension(ServerWorld world, List<String> allowedDimensions) {
        if (world == null || allowedDimensions == null || allowedDimensions.isEmpty()) {
            return false;
        }

        RegistryKey<?> worldKey = world.getRegistryKey();
        Identifier worldId = worldKey.getValue();

        return allowedDimensions.stream()
                .map(Identifier::tryParse)
                .anyMatch(id -> id != null && id.equals(worldId));
    }
}
