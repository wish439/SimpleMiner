package com.wishtoday.ts.simpleminer.mixinInterface;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface WorldChunkTestExtension {
    BlockState simpleminer$setBlockState(BlockPos pos, BlockState state, boolean moved);
}
