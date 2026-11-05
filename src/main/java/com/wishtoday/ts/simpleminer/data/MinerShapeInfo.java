package com.wishtoday.ts.simpleminer.data;

import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
@ToString
@Getter
@RequiredArgsConstructor
public class MinerShapeInfo {
    private int currentShape;
    private boolean isKeyPressed;
    @Nullable
    private ShapeResult blockPoses;
    @Nullable
    private BlockPos currentBlockPos;
    private Direction currentDirection;
    @NotNull
    private PlayerEntity player;

    public MinerShapeInfo(int currentShape, boolean isKeyPressed, PlayerEntity player) {
        this.currentShape = currentShape;
        this.isKeyPressed = isKeyPressed;
        this.player = player;
        this.blockPoses = null;
        this.currentBlockPos = null;
        this.currentDirection = null;
    }
}
