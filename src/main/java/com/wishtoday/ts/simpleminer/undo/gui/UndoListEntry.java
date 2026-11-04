package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.client.utils.RenderUtils;
import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.network.payloads.DeleteUndoC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestOpenSingleUndoScreenHandlerC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class UndoListEntry extends AlwaysSelectedEntryListWidget<UndoListEntry.Entry> {
    private static final int DELETE_SIZE = 14;
    private static final int MAX_PREVIEW_ITEMS = 3;
    private static final int PREVIEW_ITEM_SPACING = 18;

    private List<Entry> entries;
    private List<UndoDisplayInfo> source = List.of();

    public UndoListEntry(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
    }

    public void setEntries(List<UndoDisplayInfo> entries) {
        this.source = entries == null ? List.of() : entries;
        List<Entry> list = new ArrayList<>();
        for (UndoDisplayInfo info : source) {
            list.add(new Entry(info));
        }
        this.entries = list;
        this.clearEntries();
        for (Entry entry : list) {
            this.addEntry(entry);
        }
    }

    public boolean isEmpty() {
        return this.entries == null || this.entries.isEmpty();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
        // 背景由 UndoListScreen 统一绘制
    }

    @Override
    public int getRowWidth() {
        return this.width - 8;
    }

    public class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final UndoDisplayInfo info;
        private final String posText;
        private final String timeText;
        private final List<ItemStack> stacks;
        private final boolean hasRemainMaterials;
        private final UUID uuid;
        private final MinecraftClient minecraftClient;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        private int renderX;
        private int renderY;
        private int renderWidth;
        private int hoveredDeleteButton = -1;

        public Entry(UndoDisplayInfo info) {
            this.info = info;
            this.uuid = info.getUuid();
            this.stacks = info.getStacks();
            this.hasRemainMaterials = info.isHasRemainMaterials();
            this.minecraftClient = MinecraftClient.getInstance();
            this.posText = info.getText();
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(info.getTime()), ZoneId.systemDefault());
            this.timeText = dateTime.format(FORMATTER);
        }

        @Override
        public Text getNarration() {
            return Text.translatable("simpleminer.undo.list.posText", posText);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.renderX = x;
            this.renderY = y;
            this.renderWidth = entryWidth;

            // 行背景
            if ((index & 1) == 1) {
                context.fill(x, y, x + entryWidth, y + entryHeight - 2, UndoTheme.ROW_ALT);
            }
            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight - 2, UndoTheme.ROW_HOVER);
            }
            // 左侧强调条:材料未取完用警告色
            int accent = this.hasRemainMaterials ? UndoTheme.WARNING : UndoTheme.ACCENT;
            context.fill(x, y, x + 2, y + entryHeight - 2, accent);
            // 底部细分隔线
            context.fill(x, y + entryHeight - 1, x + entryWidth, y + entryHeight, 0x1AFFFFFF);

            // 坐标
            context.drawText(this.minecraftClient.textRenderer,
                    Text.translatable("simpleminer.undo.list.posText", posText), x + 8, y + 4, UndoTheme.TEXT, false);
            // 时间(小号、次要色)
            RenderUtils.drawScaleText(context, 0.75f, 0.75f, Text.of(this.timeText), x + 8, y + 16, UndoTheme.TEXT_DIM, false);

            // 右侧物品预览
            int previewRight = x + entryWidth - DELETE_SIZE - 8;
            if (this.stacks != null && !this.stacks.isEmpty()) {
                int count = Math.min(this.stacks.size(), MAX_PREVIEW_ITEMS);
                int totalSlots = count + (this.hasRemainMaterials ? 1 : 0);
                int startX = previewRight - totalSlots * PREVIEW_ITEM_SPACING + 2;
                for (int i = 0; i < count; i++) {
                    context.drawItem(this.stacks.get(i), startX + i * PREVIEW_ITEM_SPACING, y + 6);
                }
                if (this.hasRemainMaterials) {
                    context.drawText(this.minecraftClient.textRenderer, Text.of("+"),
                            startX + count * PREVIEW_ITEM_SPACING + 4, y + 9, UndoTheme.TEXT_DIM, false);
                }
            }

            // 删除按钮(常驻可发现,悬停变亮)
            int delX = x + entryWidth - DELETE_SIZE - 4;
            int delY = y + (entryHeight - DELETE_SIZE) / 2 - 1;
            boolean delHovered = mouseX >= delX && mouseX <= delX + DELETE_SIZE
                    && mouseY >= delY && mouseY <= delY + DELETE_SIZE;
            context.fill(delX, delY, delX + DELETE_SIZE, delY + DELETE_SIZE,
                    delHovered ? UndoTheme.DANGER_HOVER : (hovered ? UndoTheme.DANGER : 0x33B8434B));
            int tw = this.minecraftClient.textRenderer.getWidth("x");
            context.drawText(this.minecraftClient.textRenderer, "x",
                    delX + (DELETE_SIZE - tw) / 2, delY + (DELETE_SIZE - this.minecraftClient.textRenderer.fontHeight) / 2,
                    delHovered ? 0xFFFFFFFF : 0xFFFFD5D5, false);
        }

        private int deleteX() {
            return renderX + renderWidth - DELETE_SIZE - 4;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            int delX = deleteX();
            if (mouseX >= delX && mouseX <= delX + DELETE_SIZE) {
                ClientPlayNetworking.send(new DeleteUndoC2SPayload(this.uuid));
                ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
                return true;
            }
            ClientPlayNetworking.send(new RequestOpenSingleUndoScreenHandlerC2SPayload(this.uuid));
            return true;
        }
    }
}
