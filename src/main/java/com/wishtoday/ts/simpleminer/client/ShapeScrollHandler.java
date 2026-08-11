package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.network.LinearInfosSyncC2SPayload;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;

@Service
public class ShapeScrollHandler {
    private final Shapes shapes;

    public ShapeScrollHandler(Shapes shapes) {
        this.shapes = shapes;
    }

    public boolean onMouseScrolled(double amountX, double amountY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!SimpleminerClient.isPressing()) return false;
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        if (amountY == 0 && amountX == 0) return false;
        int delta = amountY < 0 ? 1 : -1;
        if (player.isSneaking()) {
            int current = SimpleminerClient.getShapeIndex();
            int total = this.shapes.getShapeCount();
            int newIndex = MathHelper.floorMod(current + delta, total);
            SimpleminerClient.setShapeIndex(newIndex);
            ClientPlayNetworking.send(new KeywordPressedPayload(SimpleminerClient.isPressing(), newIndex));
            return true;
        }

        //hasScrolledYet = true;
        if (InputUtil.isKeyPressed(client.getWindow().getHandle()
                , InputUtil.GLFW_KEY_LEFT_ALT)) {
            LinearShapeInfos infos = SimpleminerClient.getLinearShapeInfos();
            int height = infos.getHeight();
            int newHeight = height + delta;
            if (newHeight < 1) {
                return false;
            }
            infos.setHeight(newHeight);
            ClientPlayNetworking.send(new LinearInfosSyncC2SPayload(infos));
            return true;
        }
        if (InputUtil.isKeyPressed(client.getWindow().getHandle()
                , InputUtil.GLFW_KEY_RIGHT_ALT)) {
            LinearShapeInfos infos = SimpleminerClient.getLinearShapeInfos();
            int width = infos.getWidth();
            int newWidth = width + delta;
            if (newWidth < 1) {
                return false;
            }
            infos.setWidth(newWidth);
            ClientPlayNetworking.send(new LinearInfosSyncC2SPayload(infos));
            return true;
        }
        return false;
    }
}
