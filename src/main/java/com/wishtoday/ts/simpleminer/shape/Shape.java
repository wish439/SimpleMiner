package com.wishtoday.ts.simpleminer.shape;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;

public interface Shape {
    LongOpenHashSet walk(ShapeContext context);

    int index();

    Text getDisplayName();
}
