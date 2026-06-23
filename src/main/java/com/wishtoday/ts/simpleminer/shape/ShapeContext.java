package com.wishtoday.ts.simpleminer.shape;

import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import lombok.Data;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class ShapeContext {
    private int maxSize;
    private PlayerEntity player;
    private BlockPos currentTargetPos;
    private BlockState currentTargetState;
    private World world;
}
