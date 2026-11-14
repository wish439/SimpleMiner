package com.wishtoday.ts.simpleminer.mixinInterface;

import com.wishtoday.ts.simpleminer.debug.NanoTimerAccumulator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface WorldTestExtension {
    boolean simpleminer$setBlockStateWithOutLightUpdate(BlockPos pos, BlockState state, int flag);
    boolean simpleminer$setToAir(BlockPos pos, BlockState state, BlockState current, int flag, NanoTimerAccumulator setBlock, NanoTimerAccumulator removeBlockEntity, NanoTimerAccumulator blockAdded, NanoTimerAccumulator noForceState, NanoTimerAccumulator blockChanged, NanoTimerAccumulator insideSet, NanoTimerAccumulator syncToClient);
}
