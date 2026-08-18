package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.undo.UndoHistory;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import lombok.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Setter
@ToString
@Getter
@RequiredArgsConstructor
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

    private final UndoHistory undoHistory;

    public PlayerMinerInfo(int currentShape, boolean isKeyPressed, PlayerEntity player, IndividualConfig individualConfig) {
        this.currentShape = currentShape;
        this.isKeyPressed = isKeyPressed;
        this.player = player;
        this.undoHistory = new UndoHistory();
        this.blockPoses = null;
        this.currentBlockPos = null;
        this.currentIndividualConfig = individualConfig;
    }

}
