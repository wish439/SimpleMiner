package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private UndoStorage undoStorage;

    public PlayerMinerInfo(int currentShape, boolean isKeyPressed, PlayerEntity player, IndividualConfig individualConfig) {
        this.currentShape = currentShape;
        this.isKeyPressed = isKeyPressed;
        this.player = player;
        this.blockPoses = null;
        this.currentBlockPos = null;
        this.currentIndividualConfig = individualConfig;
        this.undoStorage = null;
    }
}
