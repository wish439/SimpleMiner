package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record BlockBreakContext(World world
        , PlayerEntity player
        , BlockPos pos
        , BlockState state
        , @Nullable BlockEntity blockEntity
        , PlayerMinerInfo info
        , ShapeResult shapeResult
        , ItemStack mainHandStack) {
}
