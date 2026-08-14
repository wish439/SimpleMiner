package com.wishtoday.ts.simpleminer.utils;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class BlockSorter {

    public static LongArrayList sortWithPlayerManhattan(LongOpenHashSet blocks, PlayerEntity player) {
        return sortWithPlayerManhattan(new LongArrayList(blocks), player);
    }

    public static LongArrayList sortWithPlayerManhattan(LongArrayList blocks, PlayerEntity player) {
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        LongComparator comparator = (a, b) -> Integer.compare(
                manhattanDistance(a, px, py, pz),
                manhattanDistance(b, px, py, pz));
        blocks.sort(comparator);
        return blocks;
    }

    public static LongArrayList sortWithPlayerSquaredEuclid(LongOpenHashSet blocks, PlayerEntity player) {
        return sortWithPlayerSquaredEuclid(new LongArrayList(blocks), player);
    }

    public static LongArrayList sortWithPlayerSquaredEuclid(LongArrayList blocks, PlayerEntity player) {
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        LongComparator comparator = (a, b) -> Long.compare(
                squaredDistance(a, px, py, pz),
                squaredDistance(b, px, py, pz));
        blocks.sort(comparator);
        return blocks;
    }

    private static int manhattanDistance(long pos, int px, int py, int pz) {
        return Math.abs(BlockPos.unpackLongX(pos) - px)
                + Math.abs(BlockPos.unpackLongY(pos) - py)
                + Math.abs(BlockPos.unpackLongZ(pos) - pz);
    }

    private static long squaredDistance(long pos, int px, int py, int pz) {
        long dx = (long) BlockPos.unpackLongX(pos) - px;
        long dy = (long) BlockPos.unpackLongY(pos) - py;
        long dz = (long) BlockPos.unpackLongZ(pos) - pz;
        return dx * dx + dy * dy + dz * dz;
    }
}
