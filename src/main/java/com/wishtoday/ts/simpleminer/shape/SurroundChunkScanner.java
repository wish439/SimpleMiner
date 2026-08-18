package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.function.Consumer;

@Service
public class SurroundChunkScanner {
    public void forEachSurroundChunk(Consumer<Chunk> consumer
            , int radiusX, int radiusZ, Chunk chunk, World world) {
        ChunkPos pos = chunk.getPos();
        int x = pos.x;
        int z = pos.z;
        int i = x + radiusX;
        int j = x - radiusX;
        int k = z + radiusZ;
        int l = z - radiusZ;
        int maxX = Math.max(i, j);
        int minX = Math.min(i, j);
        int maxZ = Math.max(k, l);
        int minZ = Math.min(k, l);
        forEachChunkPoses(maxX, maxZ, minX, minZ, p -> {
            Chunk c = world.getChunk(p.getStartPos());
            if (c == null) return;
            consumer.accept(c);
        });
    }

    private static void forEachChunkPoses(int maxX, int maxZ, int minX, int minZ, Consumer<ChunkPos> consumer) {
        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {
                consumer.accept(new ChunkPos(i, j));
            }
        }
    }
}
