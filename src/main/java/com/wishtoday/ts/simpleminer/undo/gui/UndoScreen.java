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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class UndoScreen extends HandledScreen<UndoScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("simpleminer","textures/gui/inventory.png");
    public UndoScreen(UndoScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
    }

    private ButtonWidget returnAllButton;
    private ButtonWidget undoButton;
    private MaterialListEntry listEntry;
    private boolean empty;

    @Override
    protected void init() {
        super.init();
        /*List<ItemStack> itemStacks = new ArrayList<>(List.of(new ItemStack(Items.DIAMOND)));
        itemStacks.add(new ItemStack(Items.EMERALD));
        itemStacks.add(new ItemStack(Items.GOLD_INGOT));*/
        MaterialListEntry materialListEntry = new MaterialListEntry(client, this.width / 2 - 50, this.height / 2, this.y, 20);
        UndoGuiStorageContext undoStorage = this.handler.getUndoStorage();
        materialListEntry.setEntryMapFromMaterialInfos(undoStorage.getUndoStorage());
        this.listEntry = materialListEntry;
        listEntry.setDimensionsAndPosition(this.backgroundWidth, this.backgroundHeight / 2 + 4, this.x, this.y);
        this.addDrawableChild(this.listEntry);
        int listEntryX = this.listEntry.getX();
        int listEntryY = this.listEntry.getY();
        int listEntryWidth = this.listEntry.getWidth();
        int listEntryHeight = this.listEntry.getHeight();
        ButtonWidget build = ButtonWidget.builder(Text.of("撤回"), button -> ClientPlayNetworking.send(new RequestUndoC2SPayload(this.handler.syncId)))
                .dimensions(listEntryX - listEntryWidth / 4, listEntryY, listEntryWidth / 4, listEntryHeight / 4)
                .build();
        this.undoButton = build;
        ButtonWidget build1 = ButtonWidget.builder(Text.of("归还全部材料"), this::returnAllMaterial)
                .dimensions(build.getX(), build.getY() + build.getHeight(), build.getWidth(), build.getHeight())
                .build();
        this.returnAllButton = build1;
        this.addDrawableChild(build);
        this.addDrawableChild(build1);
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
        this.listEntry.setEntryMapFromMaterialInfos(undoStorage);
        this.handler.setUndoStorage(undoStorage);
        this.empty = undoStorage.isEmpty();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        //this.listEntry.render(context, mouseX, mouseY, delta);
        this.undoButton.active = this.handler.getUndoStorage().isFully();
        this.undoButton.render(context, mouseX, mouseY, delta);
        if (empty) context.drawText(this.textRenderer, "无撤回记录", this.x, this.y, 0xFFFFFFFF, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        //context.drawTexture(TEXTURE, this.x, this.y, 0, 0, 167, 166);
        context.fill(this.x, this.y, this.x + 175, this.y + 166, 0, 0xFF888888);
        context.drawTexture(TEXTURE, this.x, this.y + 80, 0, 0, this.backgroundWidth, this.backgroundHeight);
        //context.fill(this.x, this.y, this.x + backgroundWidth, this.y + backgroundHeight, 0, 0xFFFFFFFF);
    }
}
