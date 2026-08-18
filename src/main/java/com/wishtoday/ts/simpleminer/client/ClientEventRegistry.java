package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import com.wishtoday.ts.simpleminer.shape.ClientShapeAdapter;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(condition = ClientOnlyLoadCondition.class)
public class ClientEventRegistry {
    private final Shapes shapes;
    private final Map<Class<? extends Shape>, ClientShapeAdapter> adapterMap;

    @CreateConstruction
    public ClientEventRegistry(Shapes shapes, List<ClientShapeAdapter> adapters) {
        this.shapes = shapes;
        Map<Class<? extends Shape>, ClientShapeAdapter> adapterMap = new HashMap<>();
        for (ClientShapeAdapter adapter : adapters) {
            adapterMap.put(adapter.supportedShape(), adapter);
        }
        this.adapterMap = adapterMap;
    }

    @PostConstruct
    public void register() {
        HudRenderCallback.EVENT.register(new ShapeDisplayInHud(shapes, adapterMap));
    }
}
