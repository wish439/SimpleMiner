package com.wishtoday.ts.simpleminer.core.rightClick;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public interface IndependentRightClickHandler {
    void handle(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult, boolean isOutside);
}
