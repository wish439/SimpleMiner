package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.*;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.List;

@Service
public class NetworkingRegistry {
    private final PressManager pressManager;
    private final List<ServerNetworkExtendFutures> futures;
    private final ShapeRefresher shapeRefresher;
    private final ConfigSyncPayloadHandler configSyncHandler;


    @CreateConstruction
    public NetworkingRegistry(PressManager pressManager
            , List<ServerNetworkExtendFutures> futures
            , ShapeRefresher shapeRefresher
            , ConfigSyncPayloadHandler configSyncHandler) {
        this.pressManager = pressManager;
        this.futures = futures;
        this.shapeRefresher = shapeRefresher;
        this.configSyncHandler = configSyncHandler;
    }

    @PostConstruct
    public void registerChannels() {
        ServerPlayNetworking.registerGlobalReceiver(KeywordPressedPayload.ID, this::handleKeywordPayload);
        ServerPlayNetworking.registerGlobalReceiver(SyncConfigC2SPayload.ID, this.configSyncHandler::handleSyncConfigS2C);
        ServerPlayNetworking.registerGlobalReceiver(ShapeInfosSyncC2SPayload.ID, this.configSyncHandler::handleShapeInfosSyncC2SPayload);
        this.futures.forEach(ServerNetworkExtendFutures::initialize);
    }

    private void handleKeywordPayload(KeywordPressedPayload payload, ServerPlayNetworking.Context context) {
        pressManager.togglePlayerState(payload.press(), context.player(), payload.shapeIndex());
        if (!payload.press()) {
            return;
        }
        PlayerMinerInfo info = pressManager.getPressedPlayerMinerInfo(context.player());
        if (info == null) return;
        this.shapeRefresher.refreshForce(info);
    }
}
