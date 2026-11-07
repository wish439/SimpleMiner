package com.wishtoday.ts.simpleminer.core.blockBreaker;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.util.math.ChunkSectionPos;

public class LazySectionBlockMappings implements ChunkSectionBlockMappings {
    private final LongCollection blockPoses;
    private ChunkSection2BlockMapping mapping;

    public LazySectionBlockMappings(LongCollection blockPoses) {
        this.blockPoses = blockPoses;
    }

    @Override
    public ChunkSection2BlockMapping getMapping() {
        if (this.mapping == null) {
            this.mapping = this.computeMapping();
        }
        return this.mapping;
    }

    private ChunkSection2BlockMapping computeMapping() {
        LongIterator iterator = this.blockPoses.longIterator();
        Long2ObjectOpenHashMap<LongCollection> mapping = new Long2ObjectOpenHashMap<>();
        while (iterator.hasNext()) {
            long next = iterator.nextLong();
            long l = ChunkSectionPos.fromBlockPos(next);
            mapping.computeIfAbsent(l, k -> new LongArrayList()).add(next);
        }
        return new ChunkSection2BlockMapping(mapping);
    }
}
