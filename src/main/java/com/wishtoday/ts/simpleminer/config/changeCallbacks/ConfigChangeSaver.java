package com.wishtoday.ts.simpleminer.config.changeCallbacks;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ConfigChangeCallback;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.io.PersistenceService;
import net.minecraft.server.network.ServerPlayerEntity;

@Service
public class ConfigChangeSaver implements ConfigChangeCallback {
    private final PersistenceService persistence;

    @CreateConstruction
    public ConfigChangeSaver(PersistenceService persistence) {
        this.persistence = persistence;
    }

    @Override
    public void onIndividualChange(IndividualConfig oldConfig, IndividualConfig newConfig, ServerPlayerEntity player) {
        this.persistence.saveIndividualConfigAsync(player.getUuid(), newConfig);
    }

    @Override
    public void onServerChange(ServerConfig oldConfig, ServerConfig newConfig, ServerPlayerEntity player) {
        this.persistence.saveServerConfigAsync(newConfig);
    }
}
