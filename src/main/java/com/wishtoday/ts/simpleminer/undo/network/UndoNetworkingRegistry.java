package com.wishtoday.ts.simpleminer.undo.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.io.PersistenceService;
import com.wishtoday.ts.simpleminer.undo.*;
import com.wishtoday.ts.simpleminer.network.ServerNetworkExtendFutures;
import com.wishtoday.ts.simpleminer.undo.gui.UndoGuiStorageContext;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import com.wishtoday.ts.simpleminer.undo.network.payloads.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UndoNetworkingRegistry implements ServerNetworkExtendFutures {
    private final UndoConductor undoConductor;
    private final PressManager pressManager;
    private final PersistenceService persistence;

    @CreateConstruction
    public UndoNetworkingRegistry(UndoConductor undoConductor, PressManager pressManager, PersistenceService persistence) {
        this.undoConductor = undoConductor;
        this.pressManager = pressManager;
        this.persistence = persistence;
    }

    @Override
    public void initialize() {
        ServerPlayNetworking.registerGlobalReceiver(RequestUndoC2SPayload.ID, this::handleRequestUndoPayload);
        ServerPlayNetworking.registerGlobalReceiver(TakeItemSyncC2SPayload.ID, this::handleTakeItemSyncC2SPayload);
        ServerPlayNetworking.registerGlobalReceiver(RequestReturnAllC2SPayload.ID, this::handleRequestReturnAllC2SPayload);
        ServerPlayNetworking.registerGlobalReceiver(UndoListSyncRequestC2SPayload.ID, this::handleUndoListSyncRequestC2SPayload);
        ServerPlayNetworking.registerGlobalReceiver(RequestOpenSingleUndoScreenHandlerC2SPayload.ID, this::handleRequestOpenSingleUndoScreenHandlerC2SPayload);
    }

    private void handleRequestOpenSingleUndoScreenHandlerC2SPayload(RequestOpenSingleUndoScreenHandlerC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayerEntity player = context.player();
            if (player == null) return;
            UUID uuid = payload.uuid();
            PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
            if (info == null) return;
            UndoStorage storage = info.getUndoHistory().getUndoStorage(uuid);
            if (storage != null) {
                this.openUndoScreen(player, storage, uuid);
                return;
            }
            // 已被淘汰到磁盘:异步加载后放回内存,再回到主线程打开 GUI
            this.persistence.loadUndoAsync(player.getUuid(), uuid, (loaded, error) -> {
                if (loaded == null) {
                    player.sendMessage(Text.of("There is no such UndoStorage!Please try again later."), false);
                    return;
                }
                info.getUndoHistory().addUndoStorage(loaded);
                this.openUndoScreen(player, loaded, uuid);
            });
        });
    }

    private void openUndoScreen(ServerPlayerEntity player, UndoStorage storage, UUID uuid) {
        Map<ItemStackKey, MaterialInfo> map = storage.getItems();
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player1) -> new UndoScreenHandler(syncId, playerInventory, map, this.pressManager, storage.getCompletedCount(), uuid, this.persistence), Text.of("Test")));
        ScreenHandler handler = player.currentScreenHandler;
        ServerPlayNetworking.send(player, new UndoDataSyncS2CPayload(handler.syncId, new UndoData(map, storage.getCompletedCount(), uuid)));
    }

    private void handleUndoListSyncRequestC2SPayload(UndoListSyncRequestC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayerEntity player = context.player();
            this.persistence.collectUndoList(player, list ->
                    ServerPlayNetworking.send(player, new UndoListSyncS2CPayload(list)));
        });
    }

    private void handleRequestReturnAllC2SPayload(RequestReturnAllC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ScreenHandler handler = player.currentScreenHandler;
        if (handler.syncId != payload.syncId()) {
            return;
        }
        if (!(handler instanceof UndoScreenHandler undoScreenHandler)) {
            return;
        }
        UndoGuiStorageContext undoStorage = undoScreenHandler.getUndoStorage();
        Map<ItemStackKey, MaterialInfo> map = undoStorage.getUndoStorage();
        undoConductor.returnAllMaterial(map, player);
        undoStorage.resetStorage();
    }

    private void handleTakeItemSyncC2SPayload(TakeItemSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ScreenHandler handler = player.currentScreenHandler;
        if (handler.syncId != payload.syncId()) {
            return;
        }
        if (!(handler instanceof UndoScreenHandler undoScreenHandler)) {
            return;
        }
        ItemStack cursorStack = undoScreenHandler.getCursorStack();
        if (!cursorStack.isEmpty()) return;
        if (undoScreenHandler.getUndoStorage().addCurrentCountTo(payload.key(), -payload.amount()) != -1) {
            ItemStack stack = payload.key().itemStack();
            stack.setCount(payload.amount());
            undoScreenHandler.setCursorStack(stack);
        }
    }

    private void handleRequestUndoPayload(RequestUndoC2SPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ScreenHandler handler = player.currentScreenHandler;
        int syncId = handler.syncId;
        if (payload.syncId() != syncId) return;
        if (!(handler instanceof UndoScreenHandler undoScreenHandler)) return;
        boolean fully = undoScreenHandler.getUndoStorage().isFully();
        if (!fully) return;
        this.undoConductor.undo(player, undoScreenHandler.getUuid());
        player.closeHandledScreen();
    }
}
