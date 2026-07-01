package com.wishtoday.ts.simpleminer.shape;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;

@Getter
public class ShapeResult {
    private final Set<BlockPos> blockPoses;
    private final List<BlockPos> sortedBlockPoses;

    public ShapeResult(Set<BlockPos> blockPoses, List<BlockPos> sortedBlockPoses) {
        this.blockPoses = blockPoses;
        this.sortedBlockPoses = sortedBlockPoses;
    }
}
