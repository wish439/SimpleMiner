package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.client.RenderUtils;
import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestOpenSingleUndoScreenHandlerC2SPayload;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UndoListEntry extends AlwaysSelectedEntryListWidget<UndoListEntry.Entry> {
    public UndoListEntry(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
        this.setRenderHeader(true, 15);
    }

    private List<Entry> entries;

    public void setEntries(List<UndoDisplayInfo> entries) {
        List<Entry> list = new ArrayList<>();
        for (UndoDisplayInfo entry : entries) {
            list.add(new Entry(entry.getText(), entry.getTime(), entry.getUuid(), entry.getStacks(), entry.isHasRemainMaterials()));
        }
        this.entries = list;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderEntries();
        //context.fill(mouseX, mouseY, mouseX + 100, mouseY + 100, 0xFFFFFF);
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        super.renderHeader(context, x, y);
        context.drawText(MinecraftClient.getInstance().textRenderer, "撤回列表", x, y, 0xFFFFFF, true);
    }

    private void renderEntries() {
        if (entries == null) return;
        if (entries.isEmpty()) return;
        this.clearEntries();
        for (Entry entry : entries) {
            this.addEntry(entry);
        }
    }

    /*@Override
    public int getRowWidth() {
        return this.width;
    }*/

    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final String text;
        private final String time;
        private final UUID uuid;
        private int x;
        private final MinecraftClient minecraftClient;
        private final List<ItemStack> stacks;
        private final boolean hasRemainMaterials;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

        public Entry(String text, long time, UUID uuid, List<ItemStack> stacks, boolean hasRemainMaterials) {
            this.text = text;
            this.uuid = uuid;
            this.hasRemainMaterials = hasRemainMaterials;
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(time),
                    ZoneId.systemDefault()
            );
            this.time = dateTime.format(FORMATTER);
            this.x = 0;
            this.minecraftClient = MinecraftClient.getInstance();
            this.stacks = stacks;
        }

        @Override
        public Text getNarration() {
            return Text.of("Undo List");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.x = x;
            context.drawText(minecraftClient.textRenderer, Text.of(text), x, y, 0xFFFFFF, true);
            RenderUtils.drawScaleText(context, 0.5f, 0.5f, Text.of(time), x, y + 10, 0xFFFFFF, true);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(x, y + 17, 0);
            matrices.scale(0.7f, 0.7f, 1f);
            int i = 0;
            for (ItemStack stack : stacks) {
                context.drawItem(stack, i, 0);
                i += 16;
            }
            if (this.hasRemainMaterials) {
                context.drawText(minecraftClient.textRenderer, Text.of("......"), i, 5, 0xFFFFFF, true);
            }
            matrices.pop();
            //context.drawText(minecraftClient.textRenderer, Text.of(time), x, y + 10, 0xFFFFFF, true);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ClientPlayNetworking.send(new RequestOpenSingleUndoScreenHandlerC2SPayload(this.uuid));
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            super.mouseMoved(mouseX, mouseY);
        }
    }
}
