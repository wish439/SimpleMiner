package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlockBreaker {
    private final PressManager pressManager;

    @CreateConstruction
    public BlockBreaker(PressManager pressManager) {
        this.pressManager = pressManager;
    }

    public boolean breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info == null) return true;
        Set<BlockPos> blockPoses = info.getBlockPoses();
        Object2IntOpenCustomHashMap<ItemStack> map = getItemStackObject2IntOpenCustomHashMap();
        Object2IntOpenHashMap<BlockPos> referenceCount = new Object2IntOpenHashMap<>();
        for (BlockPos posa : blockPoses) {
            int count = 0;
            for (Direction offset : Direction.values()) {
                if (blockPoses.contains(posa.offset(offset))) {
                    count++;
                }
            }
            referenceCount.put(posa, count);
        }

        Set<BlockPos> internal = referenceCount.object2IntEntrySet().stream()
                .filter(e -> e.getIntValue() >= 6)
                .map(Object2IntOpenHashMap.Entry::getKey)
                .collect(Collectors.toSet());
        for (BlockPos blockPose : blockPoses) {
            List<ItemStack> droppedStacks = Block.getDroppedStacks(world.getBlockState(blockPose), (ServerWorld) world, blockPose, world.getBlockEntity(blockPose), player, player.getMainHandStack());
            for (ItemStack stack : droppedStacks) {
                //Item item = stack.getItem();
                int count = stack.getCount();
                ItemStack key = stack.copyWithCount(1);
                if (map.containsKey(stack)) {
                    map.addTo(key, count);
                } else {
                    map.put(key, count);
                }
            }
            this.breakBlock(blockPose, world, !internal.contains(blockPose));
            //this.breakBlock(blockPose, world, true);
        }
        map.forEach((item, count) -> {
            List<ItemStack> stack = splitItemStack(item, count);
            for (ItemStack itemStack : stack) {
                Block.dropStack(world, pos, itemStack);
            }
        });
        return true;
    }


    private static @NotNull Object2IntOpenCustomHashMap<ItemStack> getItemStackObject2IntOpenCustomHashMap() {
        Hash.Strategy<ItemStack> strategy = new Hash.Strategy<>() {
            @Override
            public int hashCode(ItemStack o) {
                return ItemStack.hashCode(o);
            }

            @Override
            public boolean equals(ItemStack a, ItemStack b) {
                if (a == null && b == null) return true;
                if (a == null || b == null) return false;
                return ItemStack.areItemsAndComponentsEqual(a, b);
            }
        };

        return new Object2IntOpenCustomHashMap<>(strategy);
    }

    private List<ItemStack> splitItemStack(ItemStack item, int count) {
        List<ItemStack> stacks = new ArrayList<>();
        int max = item.getMaxCount();
        while (count > 0) {
            ItemStack stack = item.copy();
            int part = Math.min(count, max);
            stack.setCount(part);
            stacks.add(stack);
            count -= part;
        }
        return stacks;
    }


    private void breakBlock(BlockPos pos, World world, boolean update) {
        //world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
        int flag = update ? Block.NOTIFY_ALL : Block.NOTIFY_LISTENERS;
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), flag);
    }
}
