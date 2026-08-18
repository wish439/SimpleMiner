package com.wishtoday.ts.simpleminer.shape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ClientShapeAdapter {
    Class<? extends Shape> supportedShape();

    default boolean scroll(MinecraftClient client, double amountX
            , double amountY, int computedDelta) {
        if (computedDelta < 0) return this.scrollDown(computedDelta, client);
        else if (computedDelta > 0) return this.scrollUp(computedDelta, client);
        return false;
    }

    boolean scrollUp(int delta, MinecraftClient client);
    boolean scrollDown(int delta, MinecraftClient client);

    @Environment(EnvType.CLIENT)
    default List<Text> getDisplayLines() {
        return List.of();
    }
}
