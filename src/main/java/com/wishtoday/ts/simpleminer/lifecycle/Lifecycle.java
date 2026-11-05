package com.wishtoday.ts.simpleminer.lifecycle;

import net.minecraft.server.MinecraftServer;

public interface Lifecycle {
    default void onServerStarted(MinecraftServer server) {};
    default void onTick(MinecraftServer server) {};
    default void onServerEnd(MinecraftServer server) {};
}
