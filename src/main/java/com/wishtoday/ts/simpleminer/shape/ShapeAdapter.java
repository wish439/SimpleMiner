package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;

public interface ShapeAdapter {
    Class<? extends Shape> supportedShape();

    //removed, Maybe it can come back to life someday.

    /*default boolean scroll(MinecraftClient client, double amountX
            , double amountY, int computedDelta) {
        if (computedDelta < 0) return this.scrollDown(computedDelta, client);
        else if (computedDelta > 0) return this.scrollUp(computedDelta, client);
        return false;
    }

    boolean scrollUp(int delta, MinecraftClient client);
    boolean scrollDown(int delta, MinecraftClient client);*/

    default List<Text> getDisplayLines(IndividualConfig config) {
        return List.of();
    }
}
