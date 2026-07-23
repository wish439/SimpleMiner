package com.wishtoday.ts.simpleminer.shape;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

@Getter
public class ShapeResult {
    private final LongOpenHashSet blockPoses;
    private final LongArrayList sortedBlockPoses;

    public ShapeResult(LongOpenHashSet blockPoses, LongArrayList sortedBlockPoses) {
        this.blockPoses = blockPoses;
        this.sortedBlockPoses = sortedBlockPoses;
    }
}
