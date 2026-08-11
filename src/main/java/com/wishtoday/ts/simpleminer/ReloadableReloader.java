package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ServerConfig;

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
        for (Reloadable reloadable : reloadables) {
            reloadable.reload(serverConfig);
        }
    }
    public void addReloadable(Reloadable reloadable) {
        reloadables.add(reloadable);
    }
}
