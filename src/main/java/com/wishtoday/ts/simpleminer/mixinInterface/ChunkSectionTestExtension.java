package com.wishtoday.ts.simpleminer.mixinInterface;

import net.minecraft.block.BlockState;

public interface ChunkSectionTestExtension {
    BlockState simpleminer$setBlockState(int x, int y, int z, BlockState state, BlockState current, boolean lock);
}
