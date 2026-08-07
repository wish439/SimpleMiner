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
        //this.itemStacks = itemStacks;
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
    }

    public void setEntryMapFromMaterialInfos(Map<ItemStackKey, MaterialInfo> entryMap) {
        Map<ItemStackKey, Entry> map = new HashMap<>();
        for (Map.Entry<ItemStackKey, MaterialInfo> entry : entryMap.entrySet()) {
            ItemStackKey key = entry.getKey();
            MaterialInfo materialInfo = entry.getValue();
            map.put(key, new Entry(materialInfo));
        }
        this.entryMap = map;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderEntries(this.entryMap.values());
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    private void renderEntries(Collection<Entry> itemStacks) {
        this.clearEntries();
        for (Entry entry : itemStacks) {
            this.addEntry(entry);
        }
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<MaterialListEntry.Entry> {
        @Getter
        private final MaterialInfo info;
        private final MinecraftClient minecraftClient;
        private int x;

        public Entry(Item item, int maxSize, int currentSize) {
            this.info = new MaterialInfo(item, maxSize, currentSize);
            this.minecraftClient = MinecraftClient.getInstance();
            this.x = 0;
        }

        public Entry(MaterialInfo info) {
            this.info = info;
            this.minecraftClient = MinecraftClient.getInstance();
            this.x = 0;
        }

        @Override
        public Text getNarration() {
            return Text.of("Hello World!");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            ItemStack itemStack = this.info.getItemStack();
            context.drawItem(itemStack, x, y);
            this.x = x;
            String s = info.getCurrentCount() + "/" + info.getMaxCount();
            int color = info.isFinished() ? 0x00FF00 : 0xFF0000;
            context.drawText(minecraftClient.textRenderer, s, x + 32, y, color, false);
            if (!this.isMouseOver(mouseX, mouseY)) return;
            if ((this.x + 20) < mouseX) return;
            context.drawItemTooltip(minecraftClient.textRenderer, itemStack, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if ((this.x + 20) < mouseX) return super.mouseClicked(mouseX, mouseY, button);
            if (info.getCurrentCount() <= 0) return super.mouseClicked(mouseX, mouseY, button);
            ClientPlayerEntity player = minecraftClient.player;
            if (player == null) {
                return true;
            }
            ScreenHandler handler = player.currentScreenHandler;
            if (handler == null) return true;
            int min = Math.min(info.getCurrentCount(), info.getItemStack().getMaxCount());
            if (!(handler instanceof UndoScreenHandler undoScreenHandler)) return true;

            ItemStackKey key = new ItemStackKey(info.getItemStack());
            if (!undoScreenHandler.getCursorStack().isEmpty()) return true;
            if (undoScreenHandler.getUndoStorage().addCurrentCountTo(key, -min) != -1) {
                ClientPlayNetworking.send(new TakeItemSyncC2SPayload(key, min, handler.syncId));
            }
            //handler.setCursorStack();
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
