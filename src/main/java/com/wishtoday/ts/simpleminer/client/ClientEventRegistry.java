package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import com.wishtoday.ts.simpleminer.shape.ShapeAdapter;
import com.wishtoday.ts.simpleminer.shape.Shape;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(condition = ClientOnlyLoadCondition.class)
public class ClientEventRegistry {
    private final Map<Class<? extends Shape>, ShapeAdapter> adapterMap;

    @CreateConstruction
    public ClientEventRegistry(List<ShapeAdapter> adapters) {
        Map<Class<? extends Shape>, ShapeAdapter> adapterMap = new HashMap<>();
        for (ShapeAdapter adapter : adapters) {
            adapterMap.put(adapter.supportedShape(), adapter);
        }
        this.adapterMap = adapterMap;
    }

    @PostConstruct
    public void register() {
        HudRenderCallback.EVENT.register(new ShapeDisplayInHud());
    }
}
