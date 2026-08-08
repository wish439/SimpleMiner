package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.List;

@Service
public class NetworkingRegistry {
    private final ServerConfig serverConfig;
    private final PressManager pressManager;
    private final List<ServerNetworkExtendFutures> futures;
    private final ShapeRefresher shapeRefresher;

    @CreateConstruction
    public NetworkingRegistry(ServerConfig serverConfig, PressManager pressManager, List<ServerNetworkExtendFutures> futures, ShapeRefresher shapeRefresher) {
        this.serverConfig = serverConfig;
        this.pressManager = pressManager;
        this.futures = futures;
        this.shapeRefresher = shapeRefresher;
    }

    @PostConstruct
    public void registerChannels() {
        PayloadTypeRegistry.playC2S().register(KeywordPressedPayload.ID, KeywordPressedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeywordPressedPayload.ID, this::handleKeywordPayload);
        PayloadTypeRegistry.playC2S().register(SyncConfigC2SPayload.ID, SyncConfigC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SyncConfigC2SPayload.ID, this::handleSyncConfigPayload);
        this.futures.forEach(ServerNetworkExtendFutures::initialize);
    }

    private void handleSyncConfigPayload(SyncConfigC2SPayload payload, ServerPlayNetworking.Context context) {
        if (payload.type() == ConfigType.SERVER) {
            this.serverConfig.setFromConfig((ServerConfig) payload.config());
            return;
        }
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(context.player());
        info.setCurrentIndividualConfig((IndividualConfig) payload.config());
    }

    private void handleKeywordPayload(KeywordPressedPayload payload, ServerPlayNetworking.Context context) {
        pressManager.togglePlayerState(payload.press(), context.player(), payload.shapeIndex());
        if (!payload.press()) {
            return;
        }
        PlayerMinerInfo info = pressManager.getPressedPlayerMinerInfo(context.player());
        if (info == null) return;
        this.shapeRefresher.refresh(info, WorldUtils.raycast(context.player()));
    }
}
