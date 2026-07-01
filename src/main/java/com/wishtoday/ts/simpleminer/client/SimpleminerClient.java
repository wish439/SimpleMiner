package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.DummyConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
        AutoConfig.register(ServerConfig.class, DummyConfigSerializer::new);
        AutoConfig.register(IndividualConfig.class, DummyConfigSerializer::new);
    }

    private void onTick() {
        if (KeyBindings.MINE_KEY.isPressed() != pressing) {
            pressing = KeyBindings.MINE_KEY.isPressed();
            ClientPlayNetworking.send(new KeywordPressedPayload(pressing, this.shapeIndex));
        }
    }
}
