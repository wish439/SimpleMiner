package com.wishtoday.ts.simpleminer.undo.network;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.client.ClientNetworkExtendFutures;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import com.wishtoday.ts.simpleminer.undo.MaterialInfo;
import com.wishtoday.ts.simpleminer.undo.UndoData;
import com.wishtoday.ts.simpleminer.undo.gui.UndoListScreen;
import com.wishtoday.ts.simpleminer.undo.gui.UndoScreen;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoDataSyncS2CPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncS2CPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenHandler;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Service(condition = ClientOnlyLoadCondition.class)
public class UndoClientNetworkingRegistry implements ClientNetworkExtendFutures {
    @Override
    public void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(UndoDataSyncS2CPayload.ID, this::receiveUndoDataSyncPayload);
        ClientPlayNetworking.registerGlobalReceiver(UndoListSyncS2CPayload.ID, this::handleUndoListSyncS2CPayload);
    }

    private void handleUndoListSyncS2CPayload(UndoListSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            Screen screen = client.currentScreen;
            if (!(screen instanceof UndoListScreen undoListScreen)) {
                return;
            }
            undoListScreen.setEntries(payload.displayInfos());
        });
    }


    private void receiveUndoDataSyncPayload(UndoDataSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        UndoData undoData = payload.undoData();
        Map<ItemStackKey, MaterialInfo> map = undoData.map();
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
            int value = undoData.completedCount();
            undoScreenHandler.setCompletedCount(value);
            undoScreenHandler.setUuid(undoData.uuid());
            Screen screen = mc.currentScreen;
            if (!(screen instanceof UndoScreen undoScreen)) return;
            undoScreen.refreshEntryMap();
        });
    }
}
