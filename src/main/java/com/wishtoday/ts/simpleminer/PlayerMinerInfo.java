package com.wishtoday.ts.simpleminer;

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
    private final int currentShape;
    private final boolean isKeyPressed;
    @NotNull
    private final PlayerEntity player;
    @NotNull
    private Set<BlockPos> blockPoses;
    @Nullable
    private BlockPos currentBlockPos;

    public PlayerMinerInfo(int currentShape, boolean isKeyPressed, PlayerEntity player) {
        this.currentShape = currentShape;
        this.isKeyPressed = isKeyPressed;
        this.player = player;
        this.blockPoses = new HashSet<>();
        this.currentBlockPos = null;
    }
}
