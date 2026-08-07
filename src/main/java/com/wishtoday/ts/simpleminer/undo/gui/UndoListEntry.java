package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestOpenSingleUndoScreenHandlerC2SPayload;
import lombok.Setter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UndoListEntry extends AlwaysSelectedEntryListWidget<UndoListEntry.Entry> {
    public UndoListEntry(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
    }

    private List<Entry> entries;

    public void setEntries(List<UndoDisplayInfo> entries) {
        List<Entry> list = new ArrayList<>();
        for (int i1 = 0; i1 < entries.size(); i1++) {
            UndoDisplayInfo entry = entries.get(i1);
            list.add(new Entry(entry.text(), entry.time(), i1));
        }
        this.entries = list;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderEntries();
        //context.fill(mouseX, mouseY, mouseX + 100, mouseY + 100, 0xFFFFFF);
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    private void renderEntries() {
        if (entries == null) return;
        if (entries.isEmpty()) return;
        this.clearEntries();
        for (Entry entry : entries) {
            this.addEntry(entry);
        }
    }

    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final String text;
        private final String time;
        private final int index;
        private int x;
        private final MinecraftClient minecraftClient;

        public Entry(String text, long time, int index) {
            this.text = text;
            this.index = index;
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(time),
                    ZoneId.systemDefault()
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            this.time = dateTime.format(formatter);
            this.x = 0;
            this.minecraftClient = MinecraftClient.getInstance();
        }

        @Override
        public Text getNarration() {
            return Text.of("Undo List");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.x = x;
            context.drawText(minecraftClient.textRenderer,Text.of(text), x, y, 0xFFFFFF, true);
            context.drawText(minecraftClient.textRenderer,Text.of(time), x, y + 10, 0xFFFFFF, true);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if ((this.x + 100) < mouseX) return super.mouseClicked(mouseX, mouseY, button);
            ClientPlayNetworking.send(new RequestOpenSingleUndoScreenHandlerC2SPayload(this.index));
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
