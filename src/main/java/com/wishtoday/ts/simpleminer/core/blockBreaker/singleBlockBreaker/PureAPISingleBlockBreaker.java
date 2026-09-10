package com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker;

import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Name("PUREAPI")
@Service
public class PureAPISingleBlockBreaker implements SingleBlockBreaker {
    @Override
    public boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest) {
        int flag = update ? Block.NOTIFY_ALL : Block.NOTIFY_LISTENERS;
        Block block = state.getBlock();
        block.onBreak(world, pos, state, player);
        boolean b = world.setBlockState(pos, Blocks.AIR.getDefaultState(), flag);
        if (b) {
            block.onBroken(world, pos, state);
        }
        if (!player.isCreative()) {
            mainHandStack.postMine(world, state, pos, player);
            if (b && canHarvest) {
                block.afterBreak(world, player, pos, state, world.getBlockEntity(pos), mainHandStack.copy());
            }
        }
        return true;
    }
}
