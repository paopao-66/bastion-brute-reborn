package com.paopao.bastionbrutereborn.spawn;

import com.paopao.bastionbrutereborn.BastionBruteReborn;
import com.paopao.bastionbrutereborn.config.ModConfig;
import com.paopao.bastionbrutereborn.util.DebugLog;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
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

    /**
     * 在玩家附近的 Bastion Remnant 结构中查找一个候选中心点。
     * 优先使用 StructureStart 的 bounding box 中心。
     * 如果拿不到 bounding box，用 heightmap 查找地表高度替代 Y=64。
     * 搜索范围覆盖玩家附近 nearbyPlayerCheckRadius。
     */
    public static Optional<BlockPos> findCandidateBastionCenter(ServerWorld world, BlockPos near) {
        var bastion = getBastionStructure(world);
        if (bastion.isEmpty()) {
            BastionBruteReborn.LOGGER.warn("findCandidateBastionCenter：未找到堡垒结构");
            return Optional.empty();
        }

        ModConfig config = ModConfig.INSTANCE;
        int radiusChunks = Math.max(4, config.nearbyPlayerCheckRadius / 16);
        ChunkPos center = new ChunkPos(near);

        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);
                var chunk = world.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                var refs = chunk.getStructureReferences(bastion.get().value());
                if (refs.isEmpty()) continue;

                DebugLog.log("找到堡垒引用区块：({}, {})", cp.x, cp.z);

                // 尝试获取结构起点和包围盒
                StructureStart start = chunk.getStructureStarts().get(bastion.get().value());
                if (start != null) {
                    var box = start.getBoundingBox();
                    if (box != null) {
                        int centerX = (box.getMinX() + box.getMaxX()) / 2;
                        int centerZ = (box.getMinZ() + box.getMaxZ()) / 2;
                        int centerY = box.getMinY() + (box.getBlockCountY() / 2);
                        DebugLog.log(
                            "使用包围盒中心作为候选点：({}, {}, {})（包围盒 Y: {}->{}）",
                            centerX, centerY, centerZ, box.getMinY(), box.getMaxY());
                        return Optional.of(new BlockPos(centerX, centerY, centerZ));
                    }
                    DebugLog.log("获取到 StructureStart 但包围盒为空");
                } else {
                    DebugLog.log("区块 ({}, {}) 有堡垒引用但无 StructureStart", cp.x, cp.z);
                }

                // 没有 bounding box：使用 heightmap 查找地表高度
                int cx = cp.getCenterX();
                int cz = cp.getCenterZ();
                int topY = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, cx & 15, cz & 15) + 1;
                if (topY <= world.getBottomY()) {
                    topY = 64; // 极端回退
                    DebugLog.log("heightmap 无效，使用 Y=64 作为回退");
                }
                DebugLog.log("使用 heightmap 候选点：({}, {}, {})", cx, topY, cz);
                return Optional.of(new BlockPos(cx, topY, cz));
            }
        }

        DebugLog.log("在 {} 周围 {} 个区块内未找到任何堡垒引用", near, radiusChunks * 2);
        return Optional.empty();
    }
}
