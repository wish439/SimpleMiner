package com.wishtoday.ts.simpleminer.noticer.size;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;

@Service
public class MaxSizeChangedBroadcast {
    private int maxSize;
    private final PressManager pressManager;

    public MaxSizeChangedBroadcast(PressManager pressManager) {
        this.pressManager = pressManager;
        this.maxSize = -1;
    }

    private boolean canBroadcast(int newMaxSize) {
        if (this.maxSize == -1) {
            return true;
        }
        return this.maxSize != newMaxSize;
    }

    public void tryBroadcast(MinecraftServer server
            , ServerPlayerEntity modifyPlayer, int newMaxSize) {
        if (!this.canBroadcast(newMaxSize)) {
            return;
        }
        this.maxSize = newMaxSize;
        PlayerManager manager = server.getPlayerManager();
        List<ServerPlayerEntity> list = manager.getPlayerList();
        for (ServerPlayerEntity serverPlayerEntity : list) {
            PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(serverPlayerEntity);
            if (!info.getCurrentIndividualConfig().isReceiveMaxSizeUpdate()) continue;
            MutableText text = Text.stringifiedTranslatable("simpleminer.noticer.size.max_size.1", modifyPlayer.getName().getString(), maxSize);
            text.append("\n");
            MutableText translatable = Text.stringifiedTranslatable("simpleminer.noticer.size.max_size.2", info.getCurrentIndividualConfig().getPersonalMaxSize());
            text.append(translatable);
            MutableText text1 = Text.stringifiedTranslatable("simpleminer.noticer.size.max_size.3");
            text.append("\n");
            text.append(text1);
            MutableText autoSync = Text.stringifiedTranslatable("simpleminer.noticer.size.button.autosync")
                    .setStyle(
                            Style.EMPTY
                                    .withColor(Formatting.GREEN)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.stringifiedTranslatable("simpleminer.noticer.size.button.autosync.hover")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/simpleminer config individual set maxSize -1"))
                    );
            MutableText defaultValue = Text.stringifiedTranslatable("simpleminer.noticer.size.button.defaultValue")
                    .setStyle(
                            Style.EMPTY
                                    .withColor(Formatting.YELLOW)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.stringifiedTranslatable("simpleminer.noticer.size.button.defaultValue.hover")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/simpleminer config individual set maxSize 64"))
                    );

            autoSync.append(defaultValue);

            text.append("\n");
            text.append(autoSync);
            serverPlayerEntity.sendMessage(text);
        }
    }
}
