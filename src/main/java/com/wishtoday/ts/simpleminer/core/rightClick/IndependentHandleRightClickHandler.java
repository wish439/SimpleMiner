package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

@Service
public class IndependentHandleRightClickHandler implements RightClickHandler{
    @Override
    public ActionResult onUse(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult, boolean isOutside, ItemStackCollector collector) {
        return null;
    }
}
