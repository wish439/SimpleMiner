package com.wishtoday.ts.simpleminer.mixinInterface;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface WorldTestExtension {
    boolean simpleminer$setBlockStateWithOutLightUpdate(BlockPos pos, BlockState state, int flag);
}
