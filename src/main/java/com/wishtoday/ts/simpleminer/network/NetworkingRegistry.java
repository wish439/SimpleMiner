package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.ReloadableReloader;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@Service
public class NetworkingRegistry {
    private final ServerConfig serverConfig;
    private final PressManager pressManager;
    private final List<ServerNetworkExtendFutures> futures;
    private final ShapeRefresher shapeRefresher;
    private final ReloadableReloader reloader;

    @CreateConstruction
    public NetworkingRegistry(ServerConfig serverConfig, PressManager pressManager, List<ServerNetworkExtendFutures> futures, ShapeRefresher shapeRefresher, ReloadableReloader reloader) {
        this.serverConfig = serverConfig;
        this.pressManager = pressManager;
        this.futures = futures;
        this.shapeRefresher = shapeRefresher;
        this.reloader = reloader;
    }

    @PostConstruct
    public void registerChannels() {
        PayloadTypeRegistry.playC2S().register(KeywordPressedPayload.ID, KeywordPressedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeywordPressedPayload.ID, this::handleKeywordPayload);
        PayloadTypeRegistry.playC2S().register(SyncConfigC2SPayload.ID, SyncConfigC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SyncConfigC2SPayload.ID, this::handleSyncConfigPayload);
        PayloadTypeRegistry.playC2S().register(LinearInfosSyncC2SPayload.ID, LinearInfosSyncC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(LinearInfosSyncC2SPayload.ID, this::handleLinearInfosSyncC2SPayload);
        this.futures.forEach(ServerNetworkExtendFutures::initialize);
    }

    private void handleLinearInfosSyncC2SPayload(LinearInfosSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(context.player());
        if (info == null) return;
        info.setLinearShapeInfos(payload.infos());
        shapeRefresher.refreshForce(info);
    }

    private void handleSyncConfigPayload(SyncConfigC2SPayload payload, ServerPlayNetworking.Context context) {
        if (payload.type() == ConfigType.SERVER) {
            this.serverConfig.setFromConfig((ServerConfig) payload.config());
            return;
        }
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(context.player());
        info.setCurrentIndividualConfig((IndividualConfig) payload.config());
        this.reloader.reload();
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
