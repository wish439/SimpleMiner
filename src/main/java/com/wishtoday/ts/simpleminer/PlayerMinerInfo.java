package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class PlayerMinerInfo {
    private int currentShape;
    private boolean isKeyPressed;
    @NotNull
    private PlayerEntity player;
    @Nullable
    private ShapeResult blockPoses;
    @Nullable
    private BlockPos currentBlockPos;

    private IndividualConfig currentIndividualConfig;

    public PlayerMinerInfo(int currentShape, boolean isKeyPressed, PlayerEntity player, IndividualConfig individualConfig) {
        this.currentShape = currentShape;
        this.isKeyPressed = isKeyPressed;
        this.player = player;
        this.blockPoses = null;
        this.currentBlockPos = null;
        this.currentIndividualConfig = individualConfig;
    }
}
