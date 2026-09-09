package com.wishtoday.ts.simpleminer.config.changeCallbacks;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ConfigChangeCallback;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ReloadableReloader;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

@Service
public class ConfigChangeReload implements ConfigChangeCallback {
    private final ReloadableReloader reloader;

    @CreateConstruction
    public ConfigChangeReload(ReloadableReloader reloader) {
        this.reloader = reloader;
    }

    @Override
    public void onIndividualChange(IndividualConfig oldConfig, IndividualConfig newConfig, ServerPlayerEntity player) {
        this.reloader.reload();
    }

    @Override
    public void onServerChange(ServerConfig oldConfig, ServerConfig newConfig, ServerPlayerEntity player) {
        this.reloader.reload();
    }
}
