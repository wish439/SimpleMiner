package com.wishtoday.ts.simpleminer.undo.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import com.wishtoday.ts.simpleminer.network.ServerNetworkExtendFutures;
import com.wishtoday.ts.simpleminer.undo.UndoConductor;
import com.wishtoday.ts.simpleminer.undo.gui.UndoGuiStorageContext;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestReturnAllC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.RequestUndoC2SPayload;
import com.wishtoday.ts.simpleminer.undo.network.payloads.TakeItemSyncC2SPayload;
import com.wishtoday.ts.simpleminer.utils.ItemStackUtils;
import com.wishtoday.ts.simpleminer.utils.MathUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UndoNetworkingRegistry implements ServerNetworkExtendFutures {
    private final UndoConductor undoConductor;

    @CreateConstruction
    public UndoNetworkingRegistry(UndoConductor undoConductor) {
        this.undoConductor = undoConductor;
    }

    @Override
    public void initialize() {
        PayloadTypeRegistry.playC2S().register(RequestUndoC2SPayload.ID, RequestUndoC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestUndoC2SPayload.ID, this::handleRequestUndoPayload);
        PayloadTypeRegistry.playC2S().register(TakeItemSyncC2SPayload.ID, TakeItemSyncC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TakeItemSyncC2SPayload.ID, this::handleTakeItemSyncC2SPayload);
        PayloadTypeRegistry.playC2S().register(RequestReturnAllC2SPayload.ID, RequestReturnAllC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestReturnAllC2SPayload.ID, this::handleRequestReturnAllC2SPayload);
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
        this.undoConductor.undo(player);
        player.closeHandledScreen();
    }
}
