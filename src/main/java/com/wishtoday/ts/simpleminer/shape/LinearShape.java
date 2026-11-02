package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import com.wishtoday.ts.simpleminer.core.matcher.BlockMatcher;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.utils.BlockSorter;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Predicate;

@Service
public class LinearShape implements Shape {
    @Override
    public LongOpenHashSet walk(ShapeContext context) {
        Direction facing = context.getDirection();
        BlockPos currentTargetPos = context.getCurrentTargetPos();
        BlockState targetState = context.getCurrentTargetState();
        PlayerEntity player = context.getPlayer();
        long targetPosLong = currentTargetPos.asLong();
        LinearShapeInfos infos = context.getIndividualConfig().getLinearShapeInfos();
        World world = context.getWorld();
        int maxSize = context.getMaxSize();
        BlockMatcher matcher = context.getMatcher();
        LongArrayList longArrayList = this.generateLinearFromInfos(infos.getWidth(), infos.getHeight(), BlockPos.fromLong(targetPosLong), facing
                , p -> {
                    //BlockState blockState = world.getBlockState(p);
                    //return !blockState.isAir();
                    return true;
                });

        int currentStep = 0;

        LongOpenHashSet longs = new LongOpenHashSet();
        //LongOpenHashSet lastLongs = new LongOpenHashSet();
        OUTLINE:
        while (maxSize > longs.size()) {
            int size = longs.size();
            LongIterator iterator = longArrayList.longIterator();
            while (iterator.hasNext()) {
                long next = iterator.nextLong();
                long add = BlockPos.add(next, facing.getOffsetX() * currentStep, facing.getOffsetY() * currentStep, facing.getOffsetZ() * currentStep);
                /*if (!world.isChunkLoaded(BlockPos.unpackLongX(add) >> 4, BlockPos.unpackLongZ(add) >> 4)) {
                    break OUTLINE;
                }*/
                BlockState state = ((WorldExtension) world).simpleMiner$getBlockState(add);
                if (matcher.match(targetState, state)) {
                    longs.add(add);
                    if (longs.size() >= maxSize) {
                        break OUTLINE;
                    }
                }
            }
            if (size == longs.size()) {
                break OUTLINE;
            }
            currentStep++;
        }
        /*boolean willEnd;
        OUTLINE:
        while (maxSize > longs.size()) {
            LongIterator iterator = longArrayList.iterator();
            int count = 0;
            int i = longs.size() + longArrayList.size();
            willEnd = i >= maxSize;
            while (iterator.hasNext()) {
                //if (count == longArrayList.size()) break OUTLINE;
                //if (skipIndex.size() == longArrayList.size()) break OUTLINE;
                long l = iterator.nextLong();
                //if (skipIndex.contains(l)) continue;
                long add = BlockPos.add(l, facing.getOffsetX() * currentStep, facing.getOffsetY() * currentStep, facing.getOffsetZ() * currentStep);
                BlockState state = ((WorldExtension) world).simpleMiner$getBlockState(add);
                if (state.isAir() || !matcher.match(state, targetState)) {
                    //skipIndex.add(l);
                    count++;
                    if (count == longArrayList.size()) break OUTLINE;
                    continue;
                }
                if (willEnd) {
                    lastLongs.add(add);
                    continue;
                }
                if (maxSize <= longs.size()) break OUTLINE;
                longs.add(add);
            }
            LongArrayList list = BlockSorter.sortWithPlayerManhattan(lastLongs, player);
            int min = Math.min(maxSize - longs.size(), list.size());
            LongList longList = list.subList(0, min);
            longs.addAll(longList);
            lastLongs.clear();
            currentStep++;
        }*/
        return longs;
    }

    private LongArrayList generateLinearFromInfos(
            int width
            , int height
            , BlockPos initial
            , Direction currentFacing
            , Predicate<BlockPos> filter) {
        Direction.Axis[] axis = this.getDirections(currentFacing);
        height -= 1;
        width -= 1;
        double ceil = Math.ceil((double) width / 2);
        double remainWidth = width - ceil;
        BlockPos offset = initial.offset(axis[0], (int) ceil);
        BlockPos remainBlockPos = initial.offset(axis[0], -(int) remainWidth);
        double ceilHeight = Math.ceil((double) height / 2);
        double remainHeight = height - ceilHeight;
        BlockPos offsetHeight = initial.offset(axis[1], (int) ceilHeight);
        BlockPos remainHeightBlockPos = initial.offset(axis[1], -(int) remainHeight);
        BlockPos max = new BlockPos(Math.max(offset.getX(), offsetHeight.getX()), Math.max(offset.getY(), offsetHeight.getY()), Math.max(offset.getZ(), offsetHeight.getZ()));
        BlockPos min = new BlockPos(Math.min(remainBlockPos.getX(), remainHeightBlockPos.getX()), Math.min(remainBlockPos.getY(), remainHeightBlockPos.getY()), Math.min(remainBlockPos.getZ(), remainHeightBlockPos.getZ()));
        LongArrayList result = new LongArrayList();

        List<int[]> ints = this.throughFromCenter(fromBlockPos(max.subtract(initial)), fromBlockPos(min.subtract(initial)));

        for (int[] anInt : ints) {
            BlockPos add = add(initial, anInt, axis);
            if (filter.test(add)) {
                result.add(add.asLong());
            }
        }
        /*Iterable<BlockPos> iterate = BlockPos.iterate(pos, pos2);
        iterate.forEach(blockPos -> {
            if (filter.test(blockPos)) {
                result.add(blockPos.asLong());
            }
        });*/
        return result;
    }

    private BlockPos add(BlockPos pos, int[] xy, Direction.Axis[] direction) {
        BlockPos offset = pos.offset(direction[0], xy[0]);
        return offset.offset(direction[1], xy[1]);
    }

    private int[] fromBlockPos(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int a = 0, b = 0;
        if (x == 0) {
            a = y;
            b = z;
        }
        if (y == 0) {
            a = x;
            b = z;
        }
        if (z == 0) {
            a = x;
            b = y;
        }
        return xy(a, b);
    }


    private List<int[]> throughFromCenter(int[] max, int[] min) {
        List<int[]> result = new ArrayList<>();
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        LongOpenHashSet visited = new LongOpenHashSet();
        int[] start = xy(0, 0);
        queue.addLast(start);
        visited.add(toLong(start));
        result.add(start);
        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        while (!queue.isEmpty()) {
            int[] pop = queue.removeFirst();

            for (int[] d : dirs) {
                int[] next = xy(x(pop) + x(d), y(pop) + y(d));
                long key = toLong(next);

                if (inside(next, min, max) && !visited.contains(key)) {
                    visited.add(key);
                    queue.addLast(next);
                    result.add(next);
                }
            }
        }
        return result;
    }

    private long toLong(int[] a) {
        return ((long) x(a)) << 32 | ((long) y(a)) & 0xFFFFFFFFL;
    }

    private boolean inside(int[] pos, int[] min, int[] max) {
        int x = x(pos), y = y(pos);
        return x(min) <= x && x <= x(max) &&
                y(min) <= y && y <= y(max);
    }

    private int x(int[] a) {
        return a[0];
    }

    private int y(int[] a) {
        return a[1];
    }

    private int[] xy(int x, int y) {
        return new int[]{x, y};
    }

    private Direction.Axis[] getDirections(Direction facing) {
        if (facing.getAxis() == Direction.Axis.Y) {
            return new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z};
        }
        if (facing.getAxis() == Direction.Axis.Z) {
            return new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y};
        }
        return new Direction.Axis[]{Direction.Axis.Z, Direction.Axis.Y};
    }

    @Override
    public int index() {
        return 1;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("simpleminer.shape.linear");
    }
}
