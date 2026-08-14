package com.wishtoday.ts.simpleminer.utils;

import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.function.Consumer;

@Service
public class SurroundChunkScanner {
    //TODO: Complete this method
    public void forEachSurroundChunk(Consumer<Chunk> consumer
            , int length, int width, Chunk chunk, World world) {
        ChunkPos pos = chunk.getPos();
        int x = pos.x;
        int z = pos.z;

    }
}
