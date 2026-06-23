package com.wishtoday.ts.simpleminer.shape;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

@Getter
public class ShapeResult {
    private final Set<BlockPos> blockPoses;
    private final Set<BlockPos> unUpdateBlockPoses;

    public ShapeResult(Set<BlockPos> blockPoses, Set<BlockPos> unUpdateBlockPoses) {
        this.blockPoses = blockPoses;
        this.unUpdateBlockPoses = unUpdateBlockPoses;
    }
}
