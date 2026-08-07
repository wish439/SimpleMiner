package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Name("VANILLA")
@Service
public class VanillaSingleBlockBreaker implements SingleBlockBreaker {
    @Override
    public boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update) {
        if (!(player instanceof ServerPlayerEntity serverPlayerEntity)) return false;
        serverPlayerEntity.interactionManager.tryBreakBlock(pos);
        return true;
    }
}
