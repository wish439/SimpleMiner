package com.wishtoday.ts.simpleminer.utils;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class SimpleBoxMerger {

    public static List<Box> merge(LongOpenHashSet set) {
        if (set.isEmpty()) return List.of();

        List<Box> boxes = new ArrayList<>();

        while (!set.isEmpty()) {
            long seed = set.iterator().nextLong();
            BlockPos seedPos = BlockPos.fromLong(seed);

            int minX = seedPos.getX(), maxX = seedPos.getX();
            int minY = seedPos.getY(), maxY = seedPos.getY();
            int minZ = seedPos.getZ(), maxZ = seedPos.getZ();

            while (true) {
                boolean canExpand = true;
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!set.contains(BlockPos.asLong(maxX + 1, y, z))) {
                            canExpand = false;
                            break;
                        }
                    }
                    if (!canExpand) break;
                }
                if (!canExpand) break;
                maxX++;
            }
            while (true) {
                boolean canExpand = true;
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!set.contains(BlockPos.asLong(minX - 1, y, z))) {
                            canExpand = false;
                            break;
                        }
                    }
                    if (!canExpand) break;
                }
                if (!canExpand) break;
                minX--;
            }

            // 膨胀 Z+ 方向
            while (true) {
                boolean canExpand = true;
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        if (!set.contains(BlockPos.asLong(x, y, maxZ + 1))) {
                            canExpand = false;
                            break;
                        }
                    }
                    if (!canExpand) break;
                }
                if (!canExpand) break;
                maxZ++;
            }
            // 膨胀 Z- 方向
            while (true) {
                boolean canExpand = true;
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        if (!set.contains(BlockPos.asLong(x, y, minZ - 1))) {
                            canExpand = false;
                            break;
                        }
                    }
                    if (!canExpand) break;
                }
                if (!canExpand) break;
                minZ--;
            }

            // 膨胀 Y+ 方向
            while (true) {
                boolean canExpand = true;
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!set.contains(BlockPos.asLong(x, maxY + 1, z))) {
                            canExpand = false;
                            break;
                        }
                    }
                    if (!canExpand) break;
                }
                if (!canExpand) break;
                maxY++;
            }
            // 膨胀 Y- 方向
            while (true) {
                boolean canExpand = true;
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!set.contains(BlockPos.asLong(x, minY - 1, z))) {
                            canExpand = false;
                            break;
                        }
                    }
                    if (!canExpand) break;
                }
                if (!canExpand) break;
                minY--;
            }

            // 4. 把这个大方块从集合里删掉
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        set.remove(BlockPos.asLong(x, y, z));
                    }
                }
            }

            boxes.add(new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1));
        }

        return boxes;
    }
}