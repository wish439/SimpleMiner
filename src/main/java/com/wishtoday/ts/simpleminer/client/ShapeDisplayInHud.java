package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

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

        Shape shape = this.shapes.getFromIndex(SimpleminerClient.getShapeIndex());

        if (shape == null) return;
        List<Text> lines = new ArrayList<>();

        Text text = shape.getDisplayName();
        int currentBlocks = SimpleminerClient.getCurrentBlocks();

        if (currentBlocks != -1) {
            Text text1 = Text.of("将破坏" + currentBlocks + "方块");
            lines.add(text1);
        }
        lines.add(text);
        int x = 10;
        int y = 10;
        TextRenderer textRenderer = client.textRenderer;
        for (Text line : lines) {
            drawContext.drawText(textRenderer, line, x, y, 0xFFFFFF, true);
            int i = textRenderer.getWidth(line);
            y += i;
        }
    }
}
