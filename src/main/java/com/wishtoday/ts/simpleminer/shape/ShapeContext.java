package com.wishtoday.ts.simpleminer.shape;

import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import lombok.Data;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class ShapeContext {
    private final int maxSize;
    private final PlayerEntity player;
    private final BlockPos currentTargetPos;
    private final BlockState currentTargetState;
    private final World world;
    private final Direction direction;
}
