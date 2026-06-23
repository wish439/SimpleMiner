package com.wishtoday.ts.simpleminer.shape;

import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface Shape {
    Set<BlockPos> walk(ShapeContext context);

    int index();
}
