package com.wishtoday.ts.simpleminer.config.sync;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.*;
import com.wishtoday.ts.simpleminer.config.*;
import com.wishtoday.ts.simpleminer.data.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.lifecycle.Lifecycle;
import com.wishtoday.ts.simpleminer.network.ShapeInfosSyncC2SPayload;
import com.wishtoday.ts.simpleminer.network.config.OpenConfigS2CPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncIndividualConfigS2CPayload;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

@Log4j2
@Service
public class ConfigSyncHandler {
    private final ControllerManager controllerManager;
    private final ServerConfig serverConfig;
    private final ConfigChangePublisher publisher;
    @CreateConstruction
    public ConfigSyncHandler(ControllerManager pressManager, ServerConfig serverConfig, ConfigChangePublisher publisher) {
        this.controllerManager = pressManager;
        this.serverConfig = serverConfig;
        this.publisher = publisher;
    }

    public void openGUITo(ConfigType type, ServerPlayerEntity player) {
        Object config = type == ConfigType.SERVER ? this.serverConfig : this.controllerManager.getPlayerMinerInfo(player).getCurrentIndividualConfig();
        UUID uuid = this.controllerManager.getSession(player).generateUUID();
        ServerPlayNetworking.send(player, new OpenConfigS2CPayload(type, uuid, config));
    }

    private void handleUUIDNotExists(ServerPlayerEntity player, UUID uuid) {
        //Just do it not.
        log.warn("{} tried to open a non-existent config uuid.", player.getName());
    }

    public void handleSyncConfigS2C(SyncConfigC2SPayload payload, ServerPlayNetworking.Context context) {
        UUID uuid = payload.uuid();
        ServerPlayerEntity player = context.player();
        boolean b = this.controllerManager.getSession(player).consumeUUID(uuid);
        if (!b) {
            this.handleUUIDNotExists(player, uuid);
            return;
        }
        if (payload.type() == ConfigType.SERVER) {
            if (!player.hasPermissionLevel(2)) return;
            ServerConfig newServerConfig = (ServerConfig) payload.config();
            this.publisher.publishServerConfigChange(this.serverConfig, newServerConfig, player);
            this.serverConfig.setFromConfig(newServerConfig);
            return;
        }
        PlayerMinerInfo info = controllerManager.getPlayerMinerInfo(player);
        if (info == null) return;
        IndividualConfig config = (IndividualConfig) payload.config();
        config.validateAndClamp();
        this.publisher.publishIndividualConfigChange(info.getCurrentIndividualConfig(), config, player);
        info.setCurrentIndividualConfig(config);
        ServerPlayNetworking.send(player, new SyncIndividualConfigS2CPayload(config));
    }

    public void handleShapeInfosSyncC2SPayload(ShapeInfosSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        PlayerMinerInfo info = this.controllerManager.getPlayerMinerInfo(context.player());
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
