package com.wishtoday.ts.simpleminer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.undo.UndoConductor;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoDataSyncS2CPayload;
import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

@Service
public class TestCommands {
    private static final Logger log = LoggerFactory.getLogger(TestCommands.class);
    private final UndoConductor undoConductor;
    private final PressManager pressManager;

    public TestCommands(UndoConductor undoConductor, PressManager pressManager) {
        this.undoConductor = undoConductor;
        this.pressManager = pressManager;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("simpleminertest")
                .then(literal("undo")
                        .executes(this::executeUndo))
                .then(literal("tryopengui")
                        .executes(this::executeTryOpenGui))
        );
    }

    private int executeUndo(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) return -1;
        undoConductor.undo(player);
        return 1;
    }

    private int executeTryOpenGui(CommandContext<ServerCommandSource> commandContext) {
        try {
            ServerPlayerEntity player = commandContext.getSource().getPlayer();
            if (player == null) return -1;
            UndoStorage storage = this.pressManager.getPlayerMinerInfo(player).getUndoStorage();
            if (storage == null) {
                player.sendMessage(Text.of("There is no such UndoStorage!Please use simpleMiner to dig some blocks first"), false);
                return -1;
            }
            Map<ItemStackKey, MaterialInfo> map = storage.getItems();
            //Map<Item, MaterialInfo> map = this.genTestData(List.of(Items.DIAMOND, Items.EMERALD, Items.GOLD_INGOT));
            //Map<ItemStackKey, MaterialInfo> map = this.genTestDataFroStack(items);
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player1) -> new UndoScreenHandler(syncId, playerInventory, map, this.pressManager, storage.getCompletedCount()), Text.of("Test")));
            ScreenHandler handler = player.currentScreenHandler;
            ServerPlayNetworking.send(player, new UndoDataSyncS2CPayload(handler.syncId, map, storage.getCompletedCount()));
            return 1;
        } catch (Exception e) {
            log.error("e: ", e);
            throw new RuntimeException(e);
        }
    }

    private Map<ItemStackKey, MaterialInfo> genTestDataFroStack(@Nullable Object2IntOpenCustomHashMap<ItemStackKey> itemStacks) {
        Map<ItemStackKey, MaterialInfo> map = new HashMap<>();
        if (itemStacks == null) return map;
        for (Object2IntMap.Entry<ItemStackKey> entry : itemStacks.object2IntEntrySet()) {
            ItemStackKey item = entry.getKey();
            map.put(item, new MaterialInfo(item.itemStack(), entry.getIntValue(), 0));
        }
        return map;
    }

    private Map<Item, MaterialInfo> genTestData(List<Item> itemStacks) {
        Map<Item, MaterialInfo> map = new HashMap<>();
        for (Item item : itemStacks) {
            map.put(item, new MaterialInfo(item, item.getMaxCount(), 0));
        }
        return map;
    }
}
