package com.wishtoday.ts.simpleminer.config.changeCallbacks;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ConfigChangeCallback;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.shape.ShapeSyncer;
import net.minecraft.server.network.ServerPlayerEntity;

@Service
public class ConfigChangeShapeSyncer implements ConfigChangeCallback {
    private final ShapeSyncer shapeSyncer;

    @CreateConstruction
    public ConfigChangeShapeSyncer(ShapeSyncer shapeSyncer) {
        this.shapeSyncer = shapeSyncer;
    }

    @Override
    public void onIndividualChange(IndividualConfig oldConfig, IndividualConfig newConfig, ServerPlayerEntity player) {
        this.shapeSyncer.syncShapeInfoTo(player);
    }

    @Override
    public void onServerChange(ServerConfig oldConfig, ServerConfig newConfig, ServerPlayerEntity player) {

    }
}
