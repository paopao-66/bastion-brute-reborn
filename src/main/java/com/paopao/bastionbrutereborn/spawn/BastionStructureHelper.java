package com.paopao.bastionbrutereborn.spawn;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.Structure;

import java.util.Optional;

public final class BastionStructureHelper {

    private BastionStructureHelper() {}

    private static Optional<? extends RegistryEntry<Structure>> getBastionStructure(ServerWorld world) {
        var registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        return registry.getOptional(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.STRUCTURE, net.minecraft.util.Identifier.ofVanilla("bastion_remnant")));
    }

    public static boolean isPlayerInsideBastion(PlayerEntity player, ServerWorld world) {
        if (player == null || world == null) return false;

        BlockPos playerPos = player.getBlockPos();
        var bastion = getBastionStructure(world);
        if (bastion.isEmpty()) return false;

        StructureStart start = world.getStructureAccessor().getStructureAt(
            playerPos, bastion.get().value());
        return start != null && start.hasChildren();
    }

    public static Optional<BlockPos> findRandomBastionBlockPos(ServerWorld world, BlockPos near) {
        var bastion = getBastionStructure(world);
        if (bastion.isEmpty()) return Optional.empty();

        StructureStart start = world.getStructureAccessor().getStructureAt(near, bastion.get().value());
        if (start == null || !start.hasChildren()) return Optional.empty();

        return Optional.of(start.getBoundingBox().getCenter());
    }
}
