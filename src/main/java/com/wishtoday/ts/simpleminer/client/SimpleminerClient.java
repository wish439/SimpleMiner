package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.undo.gui.UndoListScreen;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.DummyConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SimpleminerClient implements ClientModInitializer {

    @Getter
    private static int shapeIndex = 0;
    @Getter
    private static boolean pressing = false;
    @Getter
    @Setter
    private static int currentBlocks = -1;

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.onTick();
        });
        AutoConfig.register(ServerConfig.class, DummyConfigSerializer::new);
        AutoConfig.register(IndividualConfig.class, DummyConfigSerializer::new);
    }

    private void onTick() {
        if (KeyBindings.MINE_KEY.isPressed() != pressing) {
            pressing = KeyBindings.MINE_KEY.isPressed();
            ClientPlayNetworking.send(new KeywordPressedPayload(pressing, shapeIndex));
            currentBlocks = -1;
        }
        if (KeyBindings.UNDO_KEY.wasPressed()) {
            MinecraftClient.getInstance().setScreen(new UndoListScreen(Text.of("撤回列表")));
            ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
        }
    }
}
