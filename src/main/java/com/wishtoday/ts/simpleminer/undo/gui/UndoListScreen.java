package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class UndoListScreen extends Screen {
    public UndoListScreen(Text title) {
        super(title);
    }

    private UndoListEntry listEntry;

    @Override
    protected void init() {
        super.init();
        this.listEntry = new UndoListEntry(MinecraftClient.getInstance(), this.width, this.height, 0, 30);
        listEntry.setDimensionsAndPosition(this.width, this.height, 0, 0);
        this.addDrawableChild(this.listEntry);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.init();
        ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
    }

    public void setEntries(List<UndoDisplayInfo> entries) {
        this.listEntry.setEntries(entries);
    }
}
