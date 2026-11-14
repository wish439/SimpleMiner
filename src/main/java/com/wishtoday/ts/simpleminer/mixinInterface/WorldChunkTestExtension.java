package com.wishtoday.ts.simpleminer.mixinInterface;

import com.wishtoday.ts.simpleminer.debug.NanoTimerAccumulator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface WorldChunkTestExtension {
    BlockState simpleminer$setBlockState(BlockPos pos, BlockState state, boolean moved);
    BlockState simpleminer$setToAir(BlockPos pos, BlockState state, BlockState current, boolean moved, NanoTimerAccumulator setBlock, NanoTimerAccumulator removeBlockEntity, NanoTimerAccumulator blockAdded);
}
