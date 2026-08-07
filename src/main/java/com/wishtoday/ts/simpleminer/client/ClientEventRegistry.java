package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@Service(condition = ClientOnlyLoadCondition.class)
public class ClientEventRegistry {
    private final Shapes shapes;

    @CreateConstruction
    public ClientEventRegistry(Shapes shapes) {
        this.shapes = shapes;
    }

    @PostConstruct
    public void register() {
        HudRenderCallback.EVENT.register(new ShapeDisplayInHud(shapes));
    }
}
