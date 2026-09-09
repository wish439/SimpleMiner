package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.*;
import com.wishtoday.ts.simpleminer.config.*;
import com.wishtoday.ts.simpleminer.io.PersistenceService;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncIndividualConfigS2CPayload;
import com.wishtoday.ts.simpleminer.noticer.MaxSizeChangedBroadcast;
import com.wishtoday.ts.simpleminer.network.shape.ShapeSyncer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

@Service
public class ConfigSyncPayloadHandler {
    private final PressManager pressManager;
    private final ServerConfig serverConfig;
    private final ConfigChangePublisher publisher;
    @CreateConstruction
    public ConfigSyncPayloadHandler(PressManager pressManager, ServerConfig serverConfig, ReloadableReloader reloader, PersistenceService persistence, MaxSizeChangedBroadcast maxSizeBroadcast, ShapeSyncer shapeSyncer, ConfigChangePublisher publisher) {
        this.pressManager = pressManager;
        this.serverConfig = serverConfig;
        this.publisher = publisher;
    }

    public void handleSyncConfigS2C(SyncConfigC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        if (payload.type() == ConfigType.SERVER) {
            if (!player.hasPermissionLevel(2)) return;
            ServerConfig newServerConfig = (ServerConfig) payload.config();
            this.publisher.publishServerConfigChange(this.serverConfig, newServerConfig, player);
            this.serverConfig.setFromConfig(newServerConfig);
            return;
        }
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        IndividualConfig config = (IndividualConfig) payload.config();
        this.publisher.publishIndividualConfigChange(info.getCurrentIndividualConfig(), config, player);
        info.setCurrentIndividualConfig(config);
        ServerPlayNetworking.send(player, new SyncIndividualConfigS2CPayload(config));
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
        this.publisher.publishIndividualConfigChange(info.getCurrentIndividualConfig(), individualConfig, context.player());
    }
}
