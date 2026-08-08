package com.wishtoday.ts.simpleminer.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RenderUtils {
    public static void drawScaleText(DrawContext context, float xScale, float yScale, Text text, int x, int y, int color, boolean shadow) {
        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(xScale, yScale, 1f);
        context.drawText(mc.textRenderer, text, 0, 0, color, shadow);
        matrices.pop();
    }
}
