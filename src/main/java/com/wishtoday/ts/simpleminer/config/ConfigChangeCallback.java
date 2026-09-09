package com.wishtoday.ts.simpleminer.config;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ConfigChangeCallback {
    void onIndividualChange(IndividualConfig oldConfig, IndividualConfig newConfig, ServerPlayerEntity player);

    void onServerChange(ServerConfig oldConfig, ServerConfig newConfig, ServerPlayerEntity player);
}
