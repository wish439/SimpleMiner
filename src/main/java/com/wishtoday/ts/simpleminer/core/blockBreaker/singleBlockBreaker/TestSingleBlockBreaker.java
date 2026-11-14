package com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker;

import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.debug.NanoTimerAccumulator;
import com.wishtoday.ts.simpleminer.debug.NanoTimerCounter;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldTestExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Service
@Name("TEST")
public class TestSingleBlockBreaker implements SingleBlockBreaker {
    @Override
    public boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest) {
        //Block block = state.getBlock();
        //block.onBreak(world, pos, state, player);
        //boolean b = ((WorldTestExtension)world).simpleminer$setToAir(pos, Blocks.AIR.getDefaultState(), state, Block.NOTIFY_LISTENERS, setBlock, removeBlockEntity, blockAdded);
        /*if (b) {
            block.onBroken(world, pos, state);
        }
        if (!player.isCreative()) {
            mainHandStack.postMine(world, state, pos, player);
            if (b && canHarvest) {
                block.afterBreak(world, player, pos, state, world.getBlockEntity(pos), mainHandStack.copy());
            }
        }*/

        return true;
    }

    @Override
    public boolean breakBlockDebug(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest, NanoTimerAccumulator setBlock, NanoTimerAccumulator removeBlockEntity, NanoTimerAccumulator blockAdded, NanoTimerAccumulator all, NanoTimerAccumulator set, NanoTimerAccumulator noForceState, NanoTimerAccumulator blockChanged) {
        NanoTimerCounter _all = new NanoTimerCounter();
        _all.startTimer();
        Block block = state.getBlock();
        block.onBreak(world, pos, state, player);
        NanoTimerCounter _set = new NanoTimerCounter();
        _set.startTimer();
        boolean b = ((WorldTestExtension)world).simpleminer$setToAir(pos, Blocks.AIR.getDefaultState(), state, Block.NOTIFY_LISTENERS, setBlock, removeBlockEntity, blockAdded, noForceState, blockChanged);
        _set.endTimer();
        if (b) {
            block.onBroken(world, pos, state);
        }
        if (!player.isCreative()) {
            mainHandStack.postMine(world, state, pos, player);
            if (b && canHarvest) {
                block.afterBreak(world, player, pos, state, world.getBlockEntity(pos), mainHandStack.copy());
            }
        }
        _all.endTimer();


        all.consume(_all.getElapsedTime());
        set.consume(_set.getElapsedTime());

        return b;
    }

    @Override
    public boolean canControlUpdate() {
        return false;
    }
}
