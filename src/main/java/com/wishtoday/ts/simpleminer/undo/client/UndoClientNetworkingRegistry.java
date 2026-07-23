package com.wishtoday.ts.simpleminer.undo.client;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import com.wishtoday.ts.simpleminer.client.ClientNetworkExtendFutures;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoDataSyncS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenHandler;

import java.util.Map;

@Service(condition = ClientOnlyLoadCondition.class)
public class UndoClientNetworkingRegistry implements ClientNetworkExtendFutures {
    @Override
    public void initialize() {
        PayloadTypeRegistry.playS2C().register(UndoDataSyncS2CPayload.ID, UndoDataSyncS2CPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(UndoDataSyncS2CPayload.ID, this::receiveUndoDataSyncPayload);
    }

    private void receiveUndoDataSyncPayload(UndoDataSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        Map<ItemStackKey, MaterialInfo> map = payload.map();
        int i = payload.syncId();
        context.client().execute(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) {
                return;
            }
            ScreenHandler handler = mc.player.currentScreenHandler;
            if (handler.syncId != i || !(handler instanceof UndoScreenHandler undoScreenHandler)) return;
            //System.out.println("AAABBBCCC:::" + map);
            undoScreenHandler.setUndoStorage(map);
            int value = payload.completedCount();
            undoScreenHandler.setCompletedCount(value);
            Screen screen = mc.currentScreen;
            if (!(screen instanceof UndoScreen undoScreen)) return;
            undoScreen.refreshEntryMap();
        });
    }
}
