package com.wishtoday.ts.simpleminer.noticer;

import com.wishtoday.ts.simpleminer.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public interface ServerConfigChangedBroadcaster {
    boolean canBroadcast(ServerPlayerEntity modifiedPlayer, ServerConfig oldConfig, ServerConfig newConfig);
    default void broadcast(ServerPlayerEntity modifiedPlayer, List<ServerPlayerEntity> allPlayers, ServerConfig oldConfig, ServerConfig newConfig) {
        if (!this.canBroadcast(modifiedPlayer, oldConfig, newConfig)) {
            return;
        }
        this.broadcastInternal(modifiedPlayer, allPlayers, oldConfig, newConfig);
    };
    void broadcastInternal(ServerPlayerEntity modifiedPlayer, List<ServerPlayerEntity> allPlayers, ServerConfig oldConfig, ServerConfig newConfig);
}
