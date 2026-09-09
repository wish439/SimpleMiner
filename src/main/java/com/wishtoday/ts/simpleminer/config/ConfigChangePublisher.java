package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@Service
public class ConfigChangePublisher {
    private final List<ConfigChangeCallback> callbacks;

    @CreateConstruction
    public ConfigChangePublisher(List<ConfigChangeCallback> callbacks) {
        this.callbacks = callbacks;
    }

    public void publishIndividualConfigChange(IndividualConfig oldConfig, IndividualConfig newConfig, ServerPlayerEntity player) {
        for (ConfigChangeCallback callback : this.callbacks) {
            callback.onIndividualChange(oldConfig, newConfig, player);
        }
    }

    public void publishServerConfigChange(ServerConfig oldConfig, ServerConfig newConfig, ServerPlayerEntity player) {
        for (ConfigChangeCallback callback : this.callbacks) {
            callback.onServerChange(oldConfig, newConfig, player);
        }
    }
}
