package com.wishtoday.ts.simpleminer.utils;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {
    @Nullable
    public static BlockPos raycast(PlayerEntity player) {
        double attributeValue = player.getAttributeValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
        if (player.isCreative()) {
            attributeValue += 0.5;
        }
        HitResult raycast = player.raycast(attributeValue, 1F, false);
        if (raycast.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return ((BlockHitResult)raycast).getBlockPos();
    }
}
