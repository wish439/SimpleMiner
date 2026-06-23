package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class SimpleminerClient implements ClientModInitializer {

    private int shapeIndex = 0;
    private boolean pressing = false;
    private BlockPos currentBlockPos = null;

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.onTick();
        });
    }

    private void onTick() {
        if (KeyBindings.MINE_KEY.isPressed() !=  pressing) {
            pressing = KeyBindings.MINE_KEY.isPressed();
            ClientPlayNetworking.send(new KeywordPressedPayload(pressing, this.shapeIndex));
        }
    }
}
