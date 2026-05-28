package com.paopao.bastionbrutereborn.spawn;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
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
        return registry.getOptional(
            RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.ofVanilla("bastion_remnant"))
        );
    }

    public static boolean isPlayerInsideBastion(PlayerEntity player, ServerWorld world) {
        if (player == null || world == null) return false;

        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        var bastion = getBastionStructure(world);
        if (bastion.isEmpty()) return false;

        // Check 3x3 chunks around the player for bastion structure references
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                var chunk = world.getChunk(chunkPos.x + dx, chunkPos.z + dz);
                var refs = chunk.getStructureReferences(bastion.get().value());
                if (!refs.isEmpty()) return true;
            }
        }
        return false;
    }

    public static Optional<BlockPos> findRandomBastionBlockPos(ServerWorld world, BlockPos near) {
        var bastion = getBastionStructure(world);
        if (bastion.isEmpty()) return Optional.empty();

        int searchRadius = 4;
        ChunkPos center = new ChunkPos(near);

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);
                var chunk = world.getChunk(cp.x, cp.z);
                var refs = chunk.getStructureReferences(bastion.get().value());
                if (!refs.isEmpty()) {
                    return Optional.of(new BlockPos(cp.getCenterX(), 64, cp.getCenterZ()));
                }
            }
        }
        return Optional.empty();
    }
}
