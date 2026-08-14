package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
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
@RequiredArgsConstructor
public class PlayerMinerInfo {
    @Getter
    private int currentShape;
    @Getter
    private boolean isKeyPressed;
    @NotNull
    @Getter
    private PlayerEntity player;
    @Nullable
    @Getter
    private ShapeResult blockPoses;
    @Nullable
    @Getter
    private BlockPos currentBlockPos;
    @Getter
    private IndividualConfig currentIndividualConfig;

    private final Map<UUID, UndoStorage> undoStorages;

    public PlayerMinerInfo(int currentShape, boolean isKeyPressed, PlayerEntity player, IndividualConfig individualConfig) {
        this.currentShape = currentShape;
        this.isKeyPressed = isKeyPressed;
        this.player = player;
        this.blockPoses = null;
        this.currentBlockPos = null;
        this.currentIndividualConfig = individualConfig;
        this.undoStorages = new HashMap<>();
    }
    public void removeUndoStorage(UUID uuid) {
        this.undoStorages.remove(uuid);
    }

    public Collection<UndoStorage> getUndoStorages() {
        return undoStorages.values();
    }

    public UndoStorage getUndoStorage(UUID uuid) {
        return this.undoStorages.get(uuid);
    }

    public void addUndoStorage(UndoStorage undoStorage) {
        this.undoStorages.put(undoStorage.getUuid(), undoStorage);
    }
}
