package com.wishtoday.ts.simpleminer.config.changeCallbacks;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ConfigChangeCallback;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.noticer.ServerConfigChangedBroadcaster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@Service
public class BroadcastDistribute implements ConfigChangeCallback {
    private final List<ServerConfigChangedBroadcaster> broadcasters;

    @CreateConstruction
    public BroadcastDistribute(List<ServerConfigChangedBroadcaster> broadcasters) {
        this.broadcasters = broadcasters;
    }

    @Override
    public void onIndividualChange(IndividualConfig oldConfig, IndividualConfig newConfig, ServerPlayerEntity player) {

    }

    @Override
    public void onServerChange(ServerConfig oldConfig, ServerConfig newConfig, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        PlayerManager manager = server.getPlayerManager();
        List<ServerPlayerEntity> playerList = manager.getPlayerList();
        if (playerList == null) return;
        if (playerList.isEmpty()) return;
        for (ServerConfigChangedBroadcaster broadcaster : this.broadcasters) {
            broadcaster.broadcast(player, playerList, oldConfig, newConfig);
        }
    }
}
