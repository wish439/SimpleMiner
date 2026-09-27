package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@Service(condition = ClientOnlyLoadCondition.class)
public class ClientEventRegistry {

    @CreateConstruction
    public ClientEventRegistry() {

    }

    @PostConstruct
    public void register() {
        HudRenderCallback.EVENT.register(new ShapeDisplayInHud());
    }
}
