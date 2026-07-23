package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.Service;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

@Service
public class ShapeAnalyzer {
    private static final Direction[] DIRECTIONS = Direction.values();

    public @NotNull LongOpenHashSet calcCompleteSurrounded(LongOpenHashSet blockPoses) {
        LongOpenHashSet longs = new LongOpenHashSet();
        for (long posa : blockPoses) {
            int count = 0;
            for (Direction offset : DIRECTIONS) {
                if (blockPoses.contains(BlockPos.add(posa, offset.getOffsetX(), offset.getOffsetY(), offset.getOffsetZ()))) {
                    count++;
                }
            }
            if (count >= 6) {
                longs.add(posa);
            }
        }

        return longs;
    }
}
