package com.wishtoday.ts.simpleminer.shape;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;

@Getter
public class ShapeResult {
    private final LongOpenHashSet blockPoses;
    private final LongList sortedBlockPoses;

    public ShapeResult(LongOpenHashSet blockPoses, LongList sortedBlockPoses) {
        this.blockPoses = blockPoses;
        this.sortedBlockPoses = sortedBlockPoses;
    }
}
