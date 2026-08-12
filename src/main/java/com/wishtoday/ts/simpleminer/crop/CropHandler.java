package com.wishtoday.ts.simpleminer.crop;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CropHandler {
    void resetAge(World world, BlockPos pos, BlockState state);
}
