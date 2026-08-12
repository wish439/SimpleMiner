package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public interface RightClickHandler {
    ActionResult onUse(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult, boolean isOutside, ItemStackCollector collector);
}
