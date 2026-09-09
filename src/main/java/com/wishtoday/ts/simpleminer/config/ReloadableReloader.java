package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;

import java.util.List;

@Service
public class ReloadableReloader {
    private final List<Reloadable> reloadables;
    private final ServerConfig serverConfig;
    public ReloadableReloader(List<Reloadable> reloadables, ServerConfig serverConfig) {
        this.reloadables = reloadables;
        this.serverConfig = serverConfig;
    }

    public void reload() {
        this.reload(this.serverConfig);
    }
    public void reload(ServerConfig config) {
        for (Reloadable reloadable : reloadables) {
            reloadable.reload(config);
        }
    }
    public void addReloadable(Reloadable reloadable) {
        reloadables.add(reloadable);
    }
}
