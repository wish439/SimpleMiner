package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import com.wishtoday.ts.simpleminer.client.SimpleminerClient;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.network.ShapeInfosSyncC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Service
public class LinearAdapter implements ShapeAdapter {

    @Override
    public Class<? extends Shape> supportedShape() {
        return LinearShape.class;
    }

    /*@Override
    public boolean scroll(MinecraftClient client, double amountX, double amountY, int computedDelta) {
        if (InputUtil.isKeyPressed(client.getWindow().getHandle()
                , InputUtil.GLFW_KEY_LEFT_ALT)) {
            LinearShapeInfos infos = SimpleminerClient.getLinearShapeInfos();
            int height = infos.getHeight();
            int newHeight = height + computedDelta;
            if (newHeight < 1) {
                return false;
            }
            infos.setHeight(newHeight);
            ClientPlayNetworking.send(new ShapeInfosSyncC2SPayload(1, infos));
            return true;
        }
        if (InputUtil.isKeyPressed(client.getWindow().getHandle()
                , InputUtil.GLFW_KEY_RIGHT_ALT)) {
            LinearShapeInfos infos = SimpleminerClient.getLinearShapeInfos();
            int width = infos.getWidth();
            int newWidth = width + computedDelta;
            if (newWidth < 1) {
                return false;
            }
            infos.setWidth(newWidth);
            ClientPlayNetworking.send(new ShapeInfosSyncC2SPayload(1, infos));
            return true;
        }
        return false;
    }

    @Override
    public boolean scrollUp(int delta, MinecraftClient client) {
        return false;
    }

    @Override
    public boolean scrollDown(int delta, MinecraftClient client) {
        return false;
    }*/

    @Override
    public List<Text> getDisplayLines(IndividualConfig config) {
        List<Text> lines = new ArrayList<>();
        LinearShapeInfos infos = config.getLinearShapeInfos();
        MutableText text1 = Text.stringifiedTranslatable("simpleminer.client.shapes.linear.widthDisplay", infos.getWidth());
        lines.add(text1);
        MutableText text2 = Text.stringifiedTranslatable("simpleminer.client.shapes.linear.heightDisplay", infos.getHeight());
        lines.add(text2);
        return lines;
    }
}
