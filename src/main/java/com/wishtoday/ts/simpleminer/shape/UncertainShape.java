package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

@Service
public class UncertainShape implements Shape {
    private final List<BlockPos> blockOffsets;
    private static final int REFERENCE_COUNT_SCOPE = 6;

    @CreateConstruction
    public UncertainShape() {
        ArrayList<BlockPos> list = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i == 0 && j == 0 && k == 0) continue;
                    list.add(new BlockPos(i, j, k));
                }
            }
        }
        this.blockOffsets = list;
    }

    @Override
    public Set<BlockPos> walk(ShapeContext context) {
        BlockPos currentTargetPos = context.getCurrentTargetPos();
        Set<BlockPos> eventually = new HashSet<>();
        Deque<BlockPos> blockPoses = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        blockPoses.push(currentTargetPos);
        eventually.add(currentTargetPos);
        //visited.add(currentTargetPos);
        BlockState matchState = context.getCurrentTargetState();
        World world = context.getWorld();
        OUTER:
        while (!blockPoses.isEmpty()) {
            BlockPos blockPos = blockPoses.pop();
            for (BlockPos blockOffset : blockOffsets) {
                BlockPos add = blockPos.add(blockOffset);
                if (visited.contains(add)) {
                    continue;
                }
                visited.add(add);
                BlockState blockState = world.getBlockState(add);
                if (blockState.isAir()) {
                    continue;
                }
                if (matchState.getBlock() != blockState.getBlock()) {
                    continue;
                }
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
}
