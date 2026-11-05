package com.wishtoday.ts.simpleminer.data;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.undo.UndoHistory;
import lombok.*;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

@Setter
@ToString
@Getter
@RequiredArgsConstructor
public class PlayerMinerInfo {
    @NotNull
    private PlayerEntity player;
    @NotNull
    private MinerShapeInfo currentMinerShapeInfo;
    @NotNull
    private IndividualConfig currentIndividualConfig;

    @NotNull
    private final UndoHistory undoHistory;
    @NotNull
    private final GuiSession session;

    public PlayerMinerInfo(int currentShape, boolean isKeyPressed, @NotNull PlayerEntity player, IndividualConfig individualConfig) {
        this.currentMinerShapeInfo = new MinerShapeInfo(currentShape, isKeyPressed, player);
        this.player = player;
        this.undoHistory = new UndoHistory();
        this.currentIndividualConfig = individualConfig;
        this.session = new GuiSession();
    }
}
