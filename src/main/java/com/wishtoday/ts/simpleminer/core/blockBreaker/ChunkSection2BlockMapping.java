package com.wishtoday.ts.simpleminer.core.blockBreaker;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import lombok.Data;

@Data
public class ChunkSection2BlockMapping {
    private final Long2ObjectOpenHashMap<LongCollection> mapping;
}
