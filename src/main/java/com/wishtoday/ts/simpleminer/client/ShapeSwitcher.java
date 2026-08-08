package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

@Service
public class ShapeSwitcher {
    private final Shapes shapes;

    public ShapeSwitcher(Shapes shapes) {
        this.shapes = shapes;
    }

    public boolean onMouseScrolled(double amountX, double amountY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!SimpleminerClient.isPressing()) return false;
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        if (!player.isSneaking()) return false;
        if (amountY != 0 || amountX != 0) {
            int delta = amountY < 0 ? 1 : -1;
            int current = SimpleminerClient.getShapeIndex();
            int total = this.shapes.getShapeCount();
            int newIndex = MathHelper.floorMod(current + delta, total);
            SimpleminerClient.setShapeIndex(newIndex);
            ClientPlayNetworking.send(new KeywordPressedPayload(SimpleminerClient.isPressing(), newIndex));
            //hasScrolledYet = true;
            return true;
        }
        return false;
    }
}
