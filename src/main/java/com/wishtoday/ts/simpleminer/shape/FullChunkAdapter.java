package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.FullChunkShapeInfos;
import com.wishtoday.ts.simpleminer.client.SimpleminerClient;
import com.wishtoday.ts.simpleminer.network.ShapeInfosSyncC2SPayload;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Service(condition = ClientOnlyLoadCondition.class)
public class FullChunkAdapter implements ClientShapeAdapter {
    @Override
    public Class<? extends Shape> supportedShape() {
        return FullChunkShape.class;
    }

    @Override
    public boolean scroll(MinecraftClient client, double amountX, double amountY, int computedDelta) {
        if (InputUtil.isKeyPressed(client.getWindow().getHandle()
                , InputUtil.GLFW_KEY_LEFT_ALT)) {
            FullChunkShapeInfos infos = SimpleminerClient.getFullChunkShapeInfos();
            int radiusX = infos.getRadiusX();
            int newRadiusX = radiusX + computedDelta;
            if (newRadiusX < 0) {
                return false;
            }
            infos.setRadiusX(newRadiusX);
            ClientPlayNetworking.send(new ShapeInfosSyncC2SPayload(2, infos));
            return true;
        }
        if (InputUtil.isKeyPressed(client.getWindow().getHandle()
                , InputUtil.GLFW_KEY_RIGHT_ALT)) {
            FullChunkShapeInfos infos = SimpleminerClient.getFullChunkShapeInfos();
            int width = infos.getRadiusZ();
            int newWidth = width + computedDelta;
            if (newWidth < 0) {
                return false;
            }
            infos.setRadiusZ(newWidth);
            ClientPlayNetworking.send(new ShapeInfosSyncC2SPayload(2, infos));
            return true;
        }
        return false;
    }

    @Override
    public List<Text> getDisplayLines() {
        List<Text> lines = new ArrayList<>();
        FullChunkShapeInfos infos = SimpleminerClient.getFullChunkShapeInfos();
        MutableText text1 = Text.stringifiedTranslatable("simpleminer.client.shapes.fullChunk.radiusXDisplay", infos.getRadiusX());
        lines.add(text1);
        MutableText text2 = Text.stringifiedTranslatable("simpleminer.client.shapes.fullChunk.radiusZDisplay", infos.getRadiusZ());
        lines.add(text2);
        return lines;
    }

    @Override
    public boolean scrollUp(int delta, MinecraftClient client) {
        return false;
    }

    @Override
    public boolean scrollDown(int delta, MinecraftClient client) {
        return false;
    }
}
