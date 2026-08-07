package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
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
        listEntry.setDimensionsAndPosition(this.width, this.height, 10, 10);
        this.addDrawableChild(this.listEntry);
    }

    public void setEntries(List<UndoDisplayInfo> entries) {
        this.listEntry.setEntries(entries);
    }
}
