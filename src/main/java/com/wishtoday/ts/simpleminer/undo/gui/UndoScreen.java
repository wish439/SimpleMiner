package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.undo.MaterialInfo;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestReturnAllC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestUndoC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class UndoScreen extends HandledScreen<UndoScreenHandler> {
    /** 面板高:原版 166 + 底部按钮/提交区 */
    private static final int PANEL_HEIGHT = 236;
    /** 材料列表区 */
    private static final int LIST_X = 8;
    private static final int LIST_Y = 26;
    private static final int LIST_WIDTH = 160;
    private static final int LIST_HEIGHT = 56;
    /** 提交槽位置(与 UndoScreenHandler 中保持一致) */
    private static final int SUBMIT_X = 8;
    private static final int SUBMIT_Y = 196;
    /** 按钮行 */
    private static final int BUTTON_Y = 170;
    private static final int BUTTON_WIDTH = 76;
    private static final int BUTTON_HEIGHT = 20;

    private ButtonWidget returnAllButton;
    private ButtonWidget undoButton;
    private MaterialListEntry listEntry;
    private boolean empty;

    public UndoScreen(UndoScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.listEntry = new MaterialListEntry(client, LIST_WIDTH, LIST_HEIGHT, LIST_Y, 20);
        this.listEntry.setDimensionsAndPosition(LIST_WIDTH, LIST_HEIGHT, this.x + LIST_X, this.y + LIST_Y);
        this.listEntry.setEntryMapFromMaterialInfos(this.handler.getUndoStorage().getUndoStorage());
        this.addDrawableChild(this.listEntry);

        this.undoButton = ButtonWidget.builder(
                        Text.translatable("simpleminer.screen.undo.undo"),
                        button -> ClientPlayNetworking.send(new RequestUndoC2SPayload(this.handler.syncId)))
                .dimensions(this.x + 8, this.y + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.returnAllButton = ButtonWidget.builder(
                        Text.translatable("simpleminer.screen.undo.returnItems"),
                        this::returnAllMaterial)
                .dimensions(this.x + 88, this.y + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addDrawableChild(this.undoButton);
        this.addDrawableChild(this.returnAllButton);

        this.playerInventoryTitleY = this.y - 94;
        this.playerInventoryTitleX = this.x;
    }

    private void returnAllMaterial(ButtonWidget buttonWidget) {
        this.handler.getUndoStorage().resetStorage();
        this.refreshEntryMap();
        ClientPlayNetworking.send(new RequestReturnAllC2SPayload(this.handler.syncId));
    }

    public void refreshEntryMap() {
        Map<ItemStackKey, MaterialInfo> undoStorage = this.handler.getUndoStorage().getUndoStorage();
        if (this.listEntry != null) {
            this.listEntry.setEntryMapFromMaterialInfos(undoStorage);
        }
        this.handler.setUndoStorage(undoStorage);
        this.empty = undoStorage.isEmpty();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean isCreative = false;
        ClientPlayerEntity player = this.client == null ? null : this.client.player;
        if (player != null) {
            isCreative = player.isCreative();
        }
        this.undoButton.active = this.handler.getUndoStorage().isFully() || isCreative;
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;
        int width = this.backgroundWidth;
        int height = this.backgroundHeight;

        // 面板
        UndoTheme.panel(context, x, y, width, height);
        UndoTheme.titleBar(context, x, y, width, 20, this.title);

        // 材料区标题与底纹
        context.drawText(this.textRenderer, Text.translatable("simpleminer.screen.undo.materials"),
                x + LIST_X, y + LIST_Y - 11, UndoTheme.TEXT_DIM, false);
        context.fill(x + LIST_X, y + LIST_Y, x + LIST_X + LIST_WIDTH, y + LIST_Y + LIST_HEIGHT, 0x0CFFFFFF);

        if (this.empty) {
            UndoTheme.centeredText(context, Text.translatable("simpleminer.screen.undo.emptyMaterial"),
                    x + LIST_X + LIST_WIDTH / 2, y + LIST_Y + LIST_HEIGHT / 2 - 4, UndoTheme.TEXT_DIM);
        }

        // 提交槽外框与说明
        UndoTheme.slotFrame(context, x + SUBMIT_X, y + SUBMIT_Y);
        context.drawText(this.textRenderer, Text.translatable("simpleminer.screen.undo.submitHint"),
                x + SUBMIT_X + 24, y + SUBMIT_Y + 4, UndoTheme.TEXT_DIM, false);

        // 按钮上方的分隔线
        UndoTheme.divider(context, x + 8, y + BUTTON_Y - 4, width - 16);
    }
}
