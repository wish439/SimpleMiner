package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

@Name("VANILLA")
@Service
public class FullVanillaRightClickHandler implements RightClickHandler{
    @Override
    public ActionResult onUse(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult, boolean isOutside) {
        return player.interactionManager.interactBlock(player, world, player.getStackInHand(hand), hand, hitResult);
    }
}
