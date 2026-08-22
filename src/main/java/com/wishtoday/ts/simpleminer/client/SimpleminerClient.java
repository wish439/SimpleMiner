package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.FullChunkShapeInfos;
import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.undo.gui.UndoListScreen;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleminerClient implements ClientModInitializer {

    @Getter
    @Setter
    private static int shapeIndex = 0;
    @Getter
    private static boolean pressing = false;
    @Getter
    @Setter
    private static int currentBlocks = -1;

    @Getter
    private static Set<BlockPos> renderBlocks;

    @Getter
    private static final LinearShapeInfos linearShapeInfos = LinearShapeInfos.DEFAULT.copy();

    @Getter
    private static final FullChunkShapeInfos fullChunkShapeInfos = FullChunkShapeInfos.DEFAULT.copy();

    public static void setRenderBlocks(Set<BlockPos> renderBlocks) {
        SimpleminerClient.renderBlocks = renderBlocks;
    }

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.onTick();
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(worldRenderEvent -> {
            if (renderBlocks == null || renderBlocks.isEmpty()) return;
            if (!pressing) {
                return;
            }
            BlockPreviewRenderer.render(renderBlocks, worldRenderEvent);
        });
    }

    public static void consumeIndividualConfig(IndividualConfig config) {
        LinearShapeInfos infos = config.getLinearShapeInfos();
        linearShapeInfos.setWidth(infos.getWidth());
        linearShapeInfos.setHeight(infos.getHeight());

        FullChunkShapeInfos shapeInfos = config.getFullChunkShapeInfos();
        fullChunkShapeInfos.setRadiusX(shapeInfos.getRadiusX());
        fullChunkShapeInfos.setRadiusZ(shapeInfos.getRadiusZ());
    }

    private void onTick() {
        if (KeyBindings.MINE_KEY.isPressed() != pressing) {
            pressing = KeyBindings.MINE_KEY.isPressed();
            ClientPlayNetworking.send(new KeywordPressedPayload(pressing, shapeIndex));
            currentBlocks = -1;
            renderBlocks = null;
        }
        if (KeyBindings.UNDO_KEY.wasPressed()) {
            if (!Screen.hasControlDown()) {
                return;
            }
            MinecraftClient.getInstance().setScreen(new UndoListScreen(Text.translatable("simpleminer.screen.undolist")));
            ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
        }
    }
}
