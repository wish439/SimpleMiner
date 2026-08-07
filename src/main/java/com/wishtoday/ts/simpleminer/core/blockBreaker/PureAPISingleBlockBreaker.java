package com.wishtoday.ts.simpleminer.core.blockBreaker;

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
    public boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update) {
        int flag = update ? Block.NOTIFY_ALL : Block.NOTIFY_LISTENERS;
        state.getBlock().onBreak(world, pos, state, player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), flag);
        if (!player.isCreative()) {
            mainHandStack.postMine(world, state, pos, player);
        }
        return true;
    }
}
