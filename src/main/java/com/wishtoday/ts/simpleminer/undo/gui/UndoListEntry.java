package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.client.RenderUtils;
import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import com.wishtoday.ts.simpleminer.undo.network.payloads.DeleteUndoC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestOpenSingleUndoScreenHandlerC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
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
        int i = 0;
        for (UndoDisplayInfo entry : entries) {
            list.add(new Entry(entry.getText(), entry.getTime(), entry.getUuid(), entry.getStacks(), entry.isHasRemainMaterials(), i));
            i++;
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
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.translatable("simpleminer.screen.undolist"), x, y, 0xFFFFFF, true);
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

    public class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final String text;
        private final String time;
        private final UUID uuid;
        private final MinecraftClient minecraftClient;
        private final List<ItemStack> stacks;
        private final boolean hasRemainMaterials;
        private final int index;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        private static final int DELETE_BUTTON_SIZE = 14;
        private int renderX;
        private int renderY;
        private int renderWidth;

        public Entry(String text, long time, UUID uuid, List<ItemStack> stacks, boolean hasRemainMaterials, int index) {
            this.text = text;
            this.uuid = uuid;
            this.hasRemainMaterials = hasRemainMaterials;
            this.index = index;
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(time),
                    ZoneId.systemDefault()
            );
            this.time = dateTime.format(FORMATTER);
            this.minecraftClient = MinecraftClient.getInstance();
            this.stacks = stacks;
        }

        @Override
        public Text getNarration() {
            return Text.of("Undo List");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.renderX = x;
            this.renderY = y;
            this.renderWidth = entryWidth;
            int i1 = (index % 2 == 0) ? 0x22C6C6C6 : 0x22808080;
            context.fill(x, y, x + entryWidth, y + entryHeight, i1);

            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0x44FFFFFF);
            }

            context.drawText(minecraftClient.textRenderer, Text.translatable("simpleminer.undo.list.posText", text), x, y, 0xFFFFFF, true);
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

            int delX = x + entryWidth - DELETE_BUTTON_SIZE - 4;
            boolean delHovered = mouseX >= delX && mouseX <= delX + DELETE_BUTTON_SIZE
                    && mouseY >= y && mouseY <= y + entryHeight;
            context.fill(delX, y + (entryHeight - DELETE_BUTTON_SIZE) / 2, delX + DELETE_BUTTON_SIZE, y + (entryHeight - DELETE_BUTTON_SIZE) / 2 + DELETE_BUTTON_SIZE, delHovered ? 0xFFCC4444 : 0xFF884444);
            context.drawText(minecraftClient.textRenderer, Text.of("X"), delX + 3, y + (entryHeight - DELETE_BUTTON_SIZE) / 2 + 2, 0xFFFFFF, true);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int delX = renderX + renderWidth - DELETE_BUTTON_SIZE - 4;
            if (button == 0 && mouseX >= delX && mouseX <= delX + DELETE_BUTTON_SIZE) {
                ClientPlayNetworking.send(new DeleteUndoC2SPayload(this.uuid));
                ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
                return true;
            }
            ClientPlayNetworking.send(new RequestOpenSingleUndoScreenHandlerC2SPayload(this.uuid));
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            super.mouseMoved(mouseX, mouseY);
        }
    }
}
