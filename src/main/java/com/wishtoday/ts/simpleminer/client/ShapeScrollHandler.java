package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import com.wishtoday.ts.simpleminer.shape.ClientShapeAdapter;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(condition = ClientOnlyLoadCondition.class)
public class ShapeScrollHandler {
    private final Shapes shapes;
    private final Map<Class<? extends Shape>, ClientShapeAdapter> adapters;

    @CreateConstruction
    public ShapeScrollHandler(Shapes shapes, List<ClientShapeAdapter> adapters) {
        this.shapes = shapes;
        Map<Class<? extends Shape>, ClientShapeAdapter> map = new HashMap<>();
        for (ClientShapeAdapter adapter : adapters) {
            map.put(adapter.supportedShape(), adapter);
        }
        this.adapters = map;
    }

    public boolean onMouseScrolled(double amountX, double amountY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!SimpleminerClient.isPressing()) return false;
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        if (amountY == 0 && amountX == 0) return false;
        int delta = amountY < 0 ? 1 : -1;
        int i = SimpleminerClient.getShapeIndex();
        if (player.isSneaking()) {
            int total = this.shapes.getShapeCount();
            int newIndex = MathHelper.floorMod(i + delta, total);
            SimpleminerClient.setShapeIndex(newIndex);
            ClientPlayNetworking.send(new KeywordPressedPayload(SimpleminerClient.isPressing(), newIndex));
            return true;
        }

        Shape shape = this.shapes.getFromIndex(i);
        if (shape == null) return false;
        ClientShapeAdapter adapter = this.adapters.get(shape.getClass());
        if (adapter == null) return false;
        return adapter.scroll(client, amountX, amountY, delta);

        //hasScrolledYet = true;

        //return false;
    }
}
