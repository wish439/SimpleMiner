package com.wishtoday.ts.simpleminer.undo.breakerFeatures;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakerFeature;
import com.wishtoday.ts.simpleminer.undo.UndoConductor;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Service
public class UndoInitializer implements BlockBreakerFeature {
    private final UndoConductor undoConductor;

    public UndoInitializer(UndoConductor undoConductor) {
        this.undoConductor = undoConductor;
    }

    @Override
    public void beforeCycle(BlockBreakContext blockBreakContext) {
        PlayerEntity player = blockBreakContext.getPlayer();
        ScreenHandler handler = player.currentScreenHandler;
        if (!(handler instanceof UndoScreenHandler undoScreenHandler)) return;
        if (this.undoConductor.returnAllMaterial(player, undoScreenHandler.getUuid())) {
            player.sendMessage(Text.of("你于上次撤回请求,提交的材料已被退回"), true);
        }
    }
}
