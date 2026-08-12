package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

@Service
public class UncertainShape implements Shape {
    private final int[][] blockOffsets;
    private static final int REFERENCE_COUNT_SCOPE = 6;

    public UncertainShape() {
        int[][] offsets = new int[26][3];
        int o = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i == 0 && j == 0 && k == 0) continue;
                    int[] offset = offsets[o++];
                    offset[0] = i;
                    offset[1] = j;
                    offset[2] = k;
                }
            }
        }
        this.blockOffsets = offsets;
    }

    @Override
    public LongOpenHashSet walk(ShapeContext context) {
        BlockPos currentTargetPos = context.getCurrentTargetPos();
        long currentTargetPosLong = currentTargetPos.asLong();
        LongOpenHashSet eventually = new LongOpenHashSet();
        LongArrayList blockPoses = new LongArrayList();
        LongOpenHashSet visited = new LongOpenHashSet();
        blockPoses.push(currentTargetPosLong);
        eventually.add(currentTargetPosLong);
        //visited.add(currentTargetPos);
        BlockState matchState = context.getCurrentTargetState();
        World world = context.getWorld();
        int cursor = 0;
        OUTER:
        while (!blockPoses.isEmpty()) {
            if (blockPoses.size() <= cursor) {
                break;
            }
            long posesLong = blockPoses.getLong(cursor++);
            for (int[] blockOffset : blockOffsets) {
                int offsetX = blockOffset[0];
                int offsetY = blockOffset[1];
                int offsetZ = blockOffset[2];
                long add = BlockPos.add(posesLong, offsetX, offsetY, offsetZ);
                if (visited.contains(add)) {
                    continue;
                }
                visited.add(add);
                BlockState blockState = ((WorldExtension) world).simpleMiner$getBlockState(BlockPos.unpackLongX(add)
                        , BlockPos.unpackLongY(add)
                        , BlockPos.unpackLongZ(add));
                if (blockState.isAir()) {
                    continue;
                }
                if (!context.getMatcher().match(matchState.getBlock(), blockState.getBlock())) {
                    continue;
                }
                /*if (matchState.getBlock() != blockState.getBlock()) {
                    continue;
                }*/
                eventually.add(add);
                if (eventually.size() >= context.getMaxSize()) {
                    break OUTER;
                }
                blockPoses.add(add);
            }
        }
        return eventually;
    }

    @Override
    public int index() {
        return 0;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("不定型");
    }
}
