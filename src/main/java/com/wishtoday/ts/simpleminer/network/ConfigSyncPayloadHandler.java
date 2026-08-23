package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.*;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.io.PersistenceService;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncIndividualConfigS2CPayload;
import com.wishtoday.ts.simpleminer.noticer.size.MaxSizeChangedBroadcast;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

@Service
public class ConfigSyncPayloadHandler {
    private final PressManager pressManager;
    private final ServerConfig serverConfig;
    private final ReloadableReloader reloader;
    private final PersistenceService persistence;
    private final MaxSizeChangedBroadcast maxSizeBroadcast;
    @CreateConstruction
    public ConfigSyncPayloadHandler(PressManager pressManager, ServerConfig serverConfig, ReloadableReloader reloader, PersistenceService persistence, MaxSizeChangedBroadcast maxSizeBroadcast) {
        this.pressManager = pressManager;
        this.serverConfig = serverConfig;
        this.reloader = reloader;
        this.persistence = persistence;
        this.maxSizeBroadcast = maxSizeBroadcast;
    }

    public void handleSyncConfigS2C(SyncConfigC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        if (payload.type() == ConfigType.SERVER) {
            if (!player.hasPermissionLevel(2)) return;
            ServerConfig newServerConfig = (ServerConfig) payload.config();
            this.serverConfig.setFromConfig(newServerConfig);
            this.persistence.saveServerConfigAsync();
            this.reloader.reload();
            this.maxSizeBroadcast.tryBroadcast(player.getServer(), player, newServerConfig.getMaxSize());
            return;
        }
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(player);
        info.setCurrentIndividualConfig((IndividualConfig) payload.config());
        this.persistence.saveIndividualConfigAsync(player);
        this.reloader.reload();
        ServerPlayNetworking.send(player, new SyncIndividualConfigS2CPayload((IndividualConfig) payload.config()));
    }

    public void handleShapeInfosSyncC2SPayload(ShapeInfosSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(context.player());
        if (info == null) return;
        IndividualConfig individualConfig = info.getCurrentIndividualConfig();
        int i = payload.shapeIndex();
        switch (i) {
            case 1 -> individualConfig.setLinearShapeInfos((LinearShapeInfos) payload.info());
            case 2 -> individualConfig.setFullChunkShapeInfos((FullChunkShapeInfos) payload.info());
        }
        this.persistence.saveIndividualConfigAsync(context.player());
    }
}
