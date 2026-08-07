package com.wishtoday.ts.simpleminer.core.blockBreaker;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SingleBlockBreaker {
    boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update);
}
