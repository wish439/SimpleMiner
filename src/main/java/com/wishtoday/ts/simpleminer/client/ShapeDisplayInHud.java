package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ShapeDisplayInHud implements HudRenderCallback {
    private final Shapes shapes;

    public ShapeDisplayInHud(Shapes shapes) {
        this.shapes = shapes;
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (!SimpleminerClient.isPressing()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int shapeIndex = SimpleminerClient.getShapeIndex();
        Shape shape = this.shapes.getFromIndex(shapeIndex);

        if (shape == null) return;
        List<Text> lines = new ArrayList<>();
        int currentBlocks = SimpleminerClient.getCurrentBlocks();
        if (currentBlocks != -1) {
            Text text1 = Text.of("将破坏" + currentBlocks + "方块");
            lines.add(text1);
        }

        Shape last = this.shapes.getLast(shape);
        if (last != null) {
            MutableText name = (MutableText) last.getDisplayName();
            name.fillStyle(Style.EMPTY
                    .withColor(Formatting.GRAY));
            lines.add(name);
        }
        Text text = shape.getDisplayName();
        lines.add(text);

        Shape next = this.shapes.getNext(shape);
        if (next != null) {
            MutableText t = (MutableText) next.getDisplayName();
            t.fillStyle(Style.EMPTY
                    .withColor(Formatting.GRAY));
            lines.add(t);
        }

        if (shapeIndex == 1) {
            LinearShapeInfos infos = SimpleminerClient.getLinearShapeInfos();
            Text text1 = Text.of("当前width:" + infos.getWidth());
            lines.add(text1);
            Text text2 = Text.of("当前height:" + infos.getHeight());
            lines.add(text2);
        }
        int x = 0;
        int y = 0;
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(10,10,0);
        TextRenderer textRenderer = client.textRenderer;
        for (Text line : lines) {
            drawContext.drawText(textRenderer, line, 0, y, 0xFFFFFF, true);
            y += textRenderer.fontHeight;
        }
        matrices.pop();
    }
}
