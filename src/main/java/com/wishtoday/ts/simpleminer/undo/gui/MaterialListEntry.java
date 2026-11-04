package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.undo.MaterialInfo;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import com.wishtoday.ts.simpleminer.undo.network.payloads.TakeItemSyncC2SPayload;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class MaterialListEntry extends AlwaysSelectedEntryListWidget<MaterialListEntry.Entry> {
    public MaterialListEntry(MinecraftClient minecraftClient, int i, int j, int k, int l) {
        super(minecraftClient, i, j, k, l);
        this.entryMap = new HashMap<>();
    }

    public MaterialListEntry(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight, List<ItemStack> itemStacks) {
        this(minecraftClient, width, height, y, itemHeight);
        Map<ItemStackKey, Entry> hashMap = new HashMap<>();
        for (ItemStack stack : itemStacks) {
            Entry entry = new Entry(stack.getItem(), stack.getCount(), 0);
            hashMap.put(new ItemStackKey(stack), entry);
        }
        this.entryMap = hashMap;
    }

    private Map<ItemStackKey, Entry> entryMap;

    public void setEntryMap(Map<ItemStackKey, Entry> entryMap) {
        this.entryMap = entryMap;
        this.rebuild();
    }

    public void setEntryMapFromMaterialInfos(Map<ItemStackKey, MaterialInfo> entryMap) {
        Map<ItemStackKey, Entry> map = new HashMap<>();
        for (Map.Entry<ItemStackKey, MaterialInfo> entry : entryMap.entrySet()) {
            map.put(entry.getKey(), new Entry(entry.getValue()));
        }
        this.entryMap = map;
        this.rebuild();
    }

    private void rebuild() {
        this.clearEntries();
        if (this.entryMap != null) {
            this.entryMap.values().stream()
                    .sorted((a, b) -> Integer.compare(b.info.getMaxCount(), a.info.getMaxCount()))
                    .forEach(this::addEntry);
        }
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
        // 背景由 UndoScreen 绘制
    }

    @Override
    public int getRowWidth() {
        return this.width - 4;
    }

    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<MaterialListEntry.Entry> {
        @Getter
        private final MaterialInfo info;
        private final MinecraftClient minecraftClient;
        private int x;
        private int renderWidth;

        public Entry(Item item, int maxSize, int currentSize) {
            this.info = new MaterialInfo(item, maxSize, currentSize);
            this.minecraftClient = MinecraftClient.getInstance();
        }

        public Entry(MaterialInfo info) {
            this.info = info;
            this.minecraftClient = MinecraftClient.getInstance();
        }

        @Override
        public Text getNarration() {
            return Text.of(this.info.getItemStack().getName().getString());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.x = x;
            this.renderWidth = entryWidth;
            int rowBottom = y + entryHeight - 1;

            if (hovered) {
                context.fill(x, y, x + entryWidth, rowBottom, UndoTheme.ROW_HOVER);
            } else if ((index & 1) == 1) {
                context.fill(x, y, x + entryWidth, rowBottom, UndoTheme.ROW_ALT);
            }

            ItemStack itemStack = this.info.getItemStack();
            context.drawItem(itemStack, x + 2, y + (entryHeight - 16) / 2);

            int current = this.info.getCurrentCount();
            int max = this.info.getMaxCount();
            boolean finished = this.info.isFinished();
            int color = finished ? UndoTheme.SUCCESS : UndoTheme.WARNING;
            Text count = Text.literal(current + " / " + max);
            context.drawText(this.minecraftClient.textRenderer, count, x + 24, y + (entryHeight - 8) / 2 - 1, color, false);

            // 进度条
            int barX = x + 84;
            int barWidth = Math.max(0, entryWidth - 90);
            if (barWidth > 8) {
                context.fill(barX, y + (entryHeight - 4) / 2, barX + barWidth, y + (entryHeight - 4) / 2 + 4, 0x33000000);
                int filled = max <= 0 ? 0 : (int) ((long) barWidth * Math.min(current, max) / max);
                context.fill(barX, y + (entryHeight - 4) / 2, barX + filled, y + (entryHeight - 4) / 2 + 4, color);
            }

            if (!this.isMouseOver(mouseX, mouseY)) return;
            if (mouseX < x + 22) return;
            context.drawItemTooltip(this.minecraftClient.textRenderer, itemStack, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            if (mouseX < this.x + 22) return false;
            if (this.info.getCurrentCount() <= 0) return false;
            ClientPlayerEntity player = minecraftClient.player;
            if (player == null) {
                return true;
            }
            ScreenHandler handler = player.currentScreenHandler;
            if (!(handler instanceof UndoScreenHandler undoScreenHandler)) return true;

            int min = Math.min(this.info.getCurrentCount(), this.info.getItemStack().getMaxCount());
            ItemStackKey key = new ItemStackKey(this.info.getItemStack());
            if (!undoScreenHandler.getCursorStack().isEmpty()) return true;
            if (undoScreenHandler.getUndoStorage().addCurrentCountTo(key, -min) != -1) {
                ClientPlayNetworking.send(new TakeItemSyncC2SPayload(key, min, handler.syncId));
            }
            return true;
        }
    }
}
