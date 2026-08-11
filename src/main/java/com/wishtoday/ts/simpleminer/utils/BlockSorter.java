package com.wishtoday.ts.simpleminer.utils;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.stream.Collectors;

public class BlockSorter {
    public static List<BlockPos> sortWithPlayerManhattan(
            List<BlockPos> blocks
            , PlayerEntity player) {
        List<Long> list = blocks.stream().map(BlockPos::asLong).toList();
        LongArrayList longs = new LongArrayList(list);
        LongArrayList longArrayList = sortWithPlayerManhattan(longs, player);
        List<BlockPos> collect = longArrayList.longStream().mapToObj(BlockPos::fromLong).collect(Collectors.toList());
        return collect;
    }

    public static LongArrayList sortWithPlayerManhattan(
            LongOpenHashSet blocks
            , PlayerEntity player) {
        LongArrayList longs = new LongArrayList(blocks);
        return sortWithPlayerManhattan(longs, player);
    }

    public static LongArrayList sortWithPlayerManhattan(
            LongArrayList blocks
            , PlayerEntity player) {
        long playerPos = player.getBlockPos().asLong();
        blocks.sort((a, b) -> {
            double da = getManhattanDistance(a, playerPos);
            double db = getManhattanDistance(b, playerPos);
            return Double.compare(da, db);
        });
        return blocks;
    }

    public static List<BlockPos> sortWithPlayerSquaredEuclid(
            List<BlockPos> blocks
            , PlayerEntity player) {
        List<Long> list = blocks.stream().map(BlockPos::asLong).toList();
        LongArrayList longs = new LongArrayList(list);
        LongArrayList longArrayList = sortWithPlayerSquaredEuclid(longs, player);
        List<BlockPos> collect = longArrayList.longStream().mapToObj(BlockPos::fromLong).collect(Collectors.toList());
        return collect;
    }

    public static LongArrayList sortWithPlayerSquaredEuclid(
            LongOpenHashSet blocks
            , PlayerEntity player) {
        LongArrayList longs = new LongArrayList(blocks);
        return sortWithPlayerSquaredEuclid(longs, player);
    }

    public static LongArrayList sortWithPlayerSquaredEuclid(
            LongArrayList blocks
            , PlayerEntity player) {
        long playerPos = player.getBlockPos().asLong();
        blocks.sort((a, b) -> {
            double da = getSquaredDistance(a, playerPos);
            double db = getSquaredDistance(b, playerPos);
            return Double.compare(da, db);
        });
        return blocks;
    }

    public static List<BlockPos> sortWithPlayerEuclid(
            List<BlockPos> blocks
            , PlayerEntity player) {
        List<Long> list = blocks.stream().map(BlockPos::asLong).toList();
        LongArrayList longs = new LongArrayList(list);
        LongArrayList longArrayList = sortWithPlayerEuclid(longs, player);
        List<BlockPos> collect = longArrayList.longStream().mapToObj(BlockPos::fromLong).collect(Collectors.toList());
        return collect;
    }

    public static LongArrayList sortWithPlayerEuclid(
            LongArrayList blocks
            , PlayerEntity player) {
        long playerPos = player.getBlockPos().asLong();
        blocks.sort((a, b) -> {
            double da = getSquaredDistance(a, playerPos);
            da = Math.sqrt(da);
            double db = getSquaredDistance(b, playerPos);
            db = Math.sqrt(db);
            return Double.compare(da, db);
        });
        return blocks;
    }

    private static int getManhattanDistance(long l1, long l2) {
        int x = BlockPos.unpackLongX(l1);
        int y = BlockPos.unpackLongY(l1);
        int z = BlockPos.unpackLongZ(l1);
        int x1 = BlockPos.unpackLongX(l2);
        int y1 = BlockPos.unpackLongY(l2);
        int z1 = BlockPos.unpackLongZ(l2);
        return getManhattanDistance(x, y, z, x1, y1, z1);
    }

    private static int getManhattanDistance(int x, int y, int z, int x1, int y1, int z1) {
        float f = Math.abs(x - x1);
        float g = Math.abs(y - y1);
        float h = Math.abs(z - z1);
        return (int)(f + g + h);
    }

    private static double getSquaredDistance(long l1, long l2) {
        int x = BlockPos.unpackLongX(l1);
        int y = BlockPos.unpackLongY(l1);
        int z = BlockPos.unpackLongZ(l1);
        int x1 = BlockPos.unpackLongX(l2);
        int y1 = BlockPos.unpackLongY(l2);
        int z1 = BlockPos.unpackLongZ(l2);
        return getSquaredDistance(x, y, z, x1, y1, z1);
    }

    private static double getSquaredDistance(double x, double y, double z, double x1, double y1, double z1) {
        double d = x1 - x;
        double e = y1 - y;
        double f = z1 - z;
        return d * d + e * e + f * f;
    }
}
