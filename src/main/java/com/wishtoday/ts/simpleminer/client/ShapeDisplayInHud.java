package com.wishtoday.ts.simpleminer.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ShapeDisplayInHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (!SimpleminerClient.isPressing()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int shapeIndex = SimpleminerClient.getShapeIndex();

        List<Text> lines = new ArrayList<>();
        int currentBlocks = SimpleminerClient.getCurrentBlocks();
        if (currentBlocks != -1) {
            MutableText text1 = Text.stringifiedTranslatable("simpleminer.client.display.willBreak", currentBlocks);
            //Text text1 = Text.of("将破坏" + currentBlocks + "方块");
            lines.add(text1);
        }

        List<Text> texts = SimpleminerClient.getShapesTexts();
        int size = texts.size() - 1;
        int last = this.getLast(shapeIndex, size);
        MutableText name = texts.get(last).copy();
        name.fillStyle(Style.EMPTY
                .withColor(Formatting.GRAY));
        lines.add(name);
        MutableText text = texts.get(shapeIndex).copy();
        lines.add(text);

        int next = this.getNext(shapeIndex, size);
        MutableText t = texts.get(next).copy();
        t.fillStyle(Style.EMPTY
                .withColor(Formatting.GRAY));
        lines.add(t);

        Int2ObjectOpenHashMap<List<Text>> otherTexts = SimpleminerClient.getOtherTexts();
        List<Text> displayLines = otherTexts.get(shapeIndex);
        if (displayLines != null) {
            lines.addAll(displayLines);
        }
        int y = 0;
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(10, 10, 0);
        TextRenderer textRenderer = client.textRenderer;
        for (Text line : lines) {
            drawContext.drawText(textRenderer, line, 0, y, 0xFFFFFF, true);
            y += textRenderer.fontHeight;
        }
        matrices.pop();
    }

    private int getNext(int index, int last) {
        int i = index + 1;
        if (i >= last) {
            i = 0;
        }
        return i;
    }

    private int getLast(int index, int last) {
        int i = index - 1;
        if (i <= 0) {
            i = last;
        }
        return i;
    }
}
