package com.wishtoday.ts.simpleminer.shape;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;

public interface Shape {
    Set<BlockPos> walk(ShapeContext context);

    int index();
}
