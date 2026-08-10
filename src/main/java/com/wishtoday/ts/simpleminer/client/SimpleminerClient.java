package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.undo.gui.UndoListScreen;
import com.wishtoday.ts.simpleminer.undo.network.payloads.UndoListSyncRequestC2SPayload;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SimpleminerClient implements ClientModInitializer {

    @Getter
    @Setter
    private static int shapeIndex = 0;
    @Getter
    private static boolean pressing = false;
    @Getter
    @Setter
    private static int currentBlocks = -1;

    public static final Event<Scroll> SCROLL_EVENT = EventFactory.createArrayBacked(Scroll.class, (listeners) -> (d1, d2) -> {
        for (Scroll listener : listeners) {
            listener.onScroll(d1, d2);
        }
    });

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.onTick();
        });
    }

    private void onTick() {
        if (KeyBindings.MINE_KEY.isPressed() != pressing) {
            pressing = KeyBindings.MINE_KEY.isPressed();
            ClientPlayNetworking.send(new KeywordPressedPayload(pressing, shapeIndex));
            currentBlocks = -1;
        }
        if (KeyBindings.UNDO_KEY.wasPressed()) {
            if (!Screen.hasControlDown()) {
                return;
            }
            MinecraftClient.getInstance().setScreen(new UndoListScreen(Text.of("撤回列表")));
            ClientPlayNetworking.send(new UndoListSyncRequestC2SPayload());
        }
    }

    public interface Scroll {
        void onScroll(double amountX, double amountY);
    }
}
