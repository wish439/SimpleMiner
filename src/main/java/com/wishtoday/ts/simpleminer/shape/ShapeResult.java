package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.ts.simpleminer.core.blockBreaker.ChunkSectionBlockMappings;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;

@Getter
public class ShapeResult {
    private final LongOpenHashSet blockPoses;
    private final LongList sortedBlockPoses;
    private final ChunkSectionBlockMappings mappings;

    public ShapeResult(LongOpenHashSet blockPoses, LongList sortedBlockPoses, ChunkSectionBlockMappings mappings) {
        this.blockPoses = blockPoses;
        this.sortedBlockPoses = sortedBlockPoses;
        this.mappings = mappings;
    }
}
