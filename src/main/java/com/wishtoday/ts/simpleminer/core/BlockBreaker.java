package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
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
    private final ShapeRefresher shapeRefresher;

    @CreateConstruction
    public BlockBreaker(PressManager pressManager, ShapeRefresher shapeRefresher) {
        this.pressManager = pressManager;
        this.shapeRefresher = shapeRefresher;
    }

    public boolean breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        ItemStack mainHandStack = player.getMainHandStack();
        PlayerMinerInfo info = this.pressManager.getPressedPlayerMinerInfo(player);
        if (info == null) return false;
        IndividualConfig individualConfig = info.getCurrentIndividualConfig();
        this.shapeRefresher.refresh(info, pos);
        ShapeResult shapeResult = info.getBlockPoses();
        if (shapeResult == null) return false;
        List<BlockPos> sortedBlockPoses = shapeResult.getSortedBlockPoses();
        Object2IntOpenCustomHashMap<ItemStack> map = getItemStackObject2IntOpenCustomHashMap();
        Set<BlockPos> internal = this.calcCompleteSurrounded(shapeResult.getBlockPoses());
        ToolComponent toolComponent = mainHandStack.get(DataComponentTypes.TOOL);
        int damagePerBlock = 0;
        if (toolComponent != null) {
            damagePerBlock = toolComponent.damagePerBlock();
        }
        int experience = 0;
        for (BlockPos blockPose : sortedBlockPoses) {
            if (!player.isCreative()) {
                if (mainHandStack.getMaxDamage() - mainHandStack.getDamage() <= damagePerBlock) {
                    if (!individualConfig.isToolPreventBroken()) mainHandStack.postMine(world, state, pos, player);
                    break;
                } else {
                    mainHandStack.postMine(world, state, pos, player);
                }
            }
            this.insertItemStack(world, player, blockPose, mainHandStack, map);
            int i = EnchantmentHelper.getBlockExperience((ServerWorld) world, mainHandStack, 0);
            experience += i;
            this.breakBlock(blockPose, world, !internal.contains(blockPose));
        }
        this.dropStacks(map, world, pos);
        if (experience <= 0) {
            return false;
        }
        ExperienceOrbEntity experienceOrbEntity = new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, experience);
        ((ServerWorld) world).spawnEntityAndPassengers(experienceOrbEntity);
        return false;
    }

    private @NotNull Set<BlockPos> calcCompleteSurrounded(Set<BlockPos> blockPoses) {
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

        return referenceCount.object2IntEntrySet().stream()
                .filter(e -> e.getIntValue() >= 6)
                .map(Object2IntOpenHashMap.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void insertItemStack(World world, PlayerEntity player, BlockPos blockPose, ItemStack mainHandStack, Object2IntOpenCustomHashMap<ItemStack> map) {
        List<ItemStack> droppedStacks = Block.getDroppedStacks(world.getBlockState(blockPose), (ServerWorld) world, blockPose, world.getBlockEntity(blockPose), player, mainHandStack);
        for (ItemStack stack : droppedStacks) {
            int count = stack.getCount();
            ItemStack key = stack.copyWithCount(1);
            if (map.containsKey(key)) {
                map.addTo(key, count);
            } else {
                map.put(key, count);
            }
        }
    }

    private void dropStacks(Object2IntOpenCustomHashMap<ItemStack> map, World world, BlockPos pos) {
        map.forEach((item, count) -> {
            List<ItemStack> stack = splitItemStack(item, count);
            for (ItemStack itemStack : stack) {
                Block.dropStack(world, pos, itemStack);
            }
        });
    }


    private @NotNull Object2IntOpenCustomHashMap<ItemStack> getItemStackObject2IntOpenCustomHashMap() {
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
