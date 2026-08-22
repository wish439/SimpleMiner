package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.network.config.OpenConfigS2CPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncIndividualConfigS2CPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestOpenSingleUndoScreenHandlerC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestReturnAllC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestUndoC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.TakeItemSyncC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoDataSyncS2CPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

@Service
public class CommonNetworkingRegistry {

    @CreateConstruction
    public CommonNetworkingRegistry() {
    }

    @PostConstruct
    public void registerAll() {
        // ===== C2S =====
        PayloadTypeRegistry.playC2S().register(KeywordPressedPayload.ID, KeywordPressedPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SyncConfigC2SPayload.ID, SyncConfigC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShapeInfosSyncC2SPayload.ID, ShapeInfosSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestUndoC2SPayload.ID, RequestUndoC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TakeItemSyncC2SPayload.ID, TakeItemSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestReturnAllC2SPayload.ID, RequestReturnAllC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UndoListSyncRequestC2SPayload.ID, UndoListSyncRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestOpenSingleUndoScreenHandlerC2SPayload.ID, RequestOpenSingleUndoScreenHandlerC2SPayload.CODEC);

        // ===== S2C =====
        PayloadTypeRegistry.playS2C().register(OpenConfigS2CPayload.ID, OpenConfigS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MineBlockSyncS2CPayload.ID, MineBlockSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncIndividualConfigS2CPayload.ID, SyncIndividualConfigS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UndoDataSyncS2CPayload.ID, UndoDataSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UndoListSyncS2CPayload.ID, UndoListSyncS2CPayload.CODEC);
    }
}
