package com.wishtoday.ts.simpleminer.mixinInterface;

import net.minecraft.block.BlockState;

public interface WorldExtension {
    BlockState simpleMiner$getBlockState(int x, int y, int z);
}
