package com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker;

import com.wishtoday.ts.simpleminer.debug.NanoTimerAccumulator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SingleBlockBreaker {
    boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest);
    default boolean breakBlockDebug(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest, NanoTimerAccumulator setBlock, NanoTimerAccumulator removeBlockEntity, NanoTimerAccumulator blockAdded, NanoTimerAccumulator all, NanoTimerAccumulator set, NanoTimerAccumulator noForceState, NanoTimerAccumulator blockChanged) {
        return true;
    }
    boolean canControlUpdate();
}
