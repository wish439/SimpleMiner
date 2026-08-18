package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.shape.ClientShapeAdapter;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeDisplayInHud implements HudRenderCallback {
    private final Shapes shapes;
    private final Map<Class<? extends Shape>, ClientShapeAdapter> adapters;

    public ShapeDisplayInHud(Shapes shapes, Map<Class<? extends Shape>, ClientShapeAdapter> adapters) {
        this.shapes = shapes;
        this.adapters = adapters;
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
            MutableText text1 = Text.stringifiedTranslatable("simpleminer.client.display.willBreak", currentBlocks);
            //Text text1 = Text.of("将破坏" + currentBlocks + "方块");
            lines.add(text1);
        }

        Shape last = this.getLast(shape, shapes);
        if (last != null) {
            MutableText name = (MutableText) last.getDisplayName();
            name.fillStyle(Style.EMPTY
                    .withColor(Formatting.GRAY));
            lines.add(name);
        }
        Text text = shape.getDisplayName();
        lines.add(text);

        Shape next = this.getNext(shape, shapes);
        if (next != null) {
            MutableText t = (MutableText) next.getDisplayName();
            t.fillStyle(Style.EMPTY
                    .withColor(Formatting.GRAY));
            lines.add(t);
        }

        ClientShapeAdapter adapter = this.adapters.get(shape.getClass());
        if (adapter != null) {
            List<Text> displayLines = adapter.getDisplayLines();
            lines.addAll(displayLines);
        }
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

    private Shape getNext(Shape shape, Shapes shapes) {
        if (shape == null) {
            return null;
        }
        int k = shape.index() + 1;
        if (k >= shapes.getShapeCount()) {
            k = 0;
        }
        return shapes.getFromIndex(k);
    }

    private Shape getLast(Shape shape, Shapes shapes) {
        if (shape == null) {
            return null;
        }
        int index = shape.index();
        if (index == 0) {
            index = shapes.getShapeCount();
        }
        return shapes.getFromIndex(index - 1);
    }
}
