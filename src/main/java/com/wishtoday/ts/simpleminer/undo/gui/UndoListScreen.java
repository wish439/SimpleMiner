package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

@Environment(EnvType.CLIENT)
public class UndoListScreen extends Screen {
    private static final int HEADER_HEIGHT = 26;
    private static final int PADDING = 8;
    private static final int CLOSE_SIZE = 14;
    private static final int FOOTER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 28;

    private final Text panelTitle;
    private UndoListEntry listEntry;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int closeX;
    private int closeY;
    private boolean closeHovered;

    public UndoListScreen(Text title) {
        super(title);
        this.panelTitle = title;
    }

    @Override
    protected void init() {
        super.init();
        this.panelWidth = Math.min(this.width - 40, 380);
        this.panelHeight = Math.min(this.height - 40, 300);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
        this.closeX = this.panelX + this.panelWidth - PADDING - CLOSE_SIZE;
        this.closeY = this.panelY + (HEADER_HEIGHT - CLOSE_SIZE) / 2;

        int listWidth = this.panelWidth - PADDING * 2;
        int listHeight = this.panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PADDING;
        this.listEntry = new UndoListEntry(MinecraftClient.getInstance(), listWidth, listHeight, this.panelY + HEADER_HEIGHT, ROW_HEIGHT);
        this.listEntry.setDimensionsAndPosition(listWidth, listHeight, this.panelX + PADDING, this.panelY + HEADER_HEIGHT);
        this.addDrawableChild(this.listEntry);

        // 打开界面即请求一次列表数据
        ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.init();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        this.closeHovered = mouseX >= closeX && mouseX <= closeX + CLOSE_SIZE
                && mouseY >= closeY && mouseY <= closeY + CLOSE_SIZE;

        UndoTheme.panel(context, panelX, panelY, panelWidth, panelHeight);
        UndoTheme.titleBar(context, panelX, panelY, panelWidth, HEADER_HEIGHT, panelTitle);
        UndoTheme.closeButton(context, closeX, closeY, CLOSE_SIZE, closeHovered);

        // 列表区域底纹
        int listTop = panelY + HEADER_HEIGHT;
        int listBottom = panelY + panelHeight - FOOTER_HEIGHT;
        context.fill(panelX + PADDING, listTop, panelX + panelWidth - PADDING, listBottom, 0x0CFFFFFF);

        // 空状态提示
        if (listEntry != null && listEntry.isEmpty()) {
            UndoTheme.centeredText(context, Text.translatable("simpleminer.screen.undolist.empty"),
                    panelX + panelWidth / 2, panelY + (HEADER_HEIGHT + panelHeight - FOOTER_HEIGHT) / 2 - 4,
                    UndoTheme.TEXT_DIM);
        }

        // 底部操作提示
        UndoTheme.divider(context, panelX + PADDING, listBottom, panelWidth - PADDING * 2);
        UndoTheme.centeredText(context, Text.translatable("simpleminer.screen.undolist.hint"),
                panelX + panelWidth / 2, listBottom + 4, UndoTheme.TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= closeX && mouseX <= closeX + CLOSE_SIZE
                && mouseY >= closeY && mouseY <= closeY + CLOSE_SIZE) {
            this.close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setEntries(List<UndoDisplayInfo> entries) {
        if (this.listEntry != null) {
            this.listEntry.setEntries(entries);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
