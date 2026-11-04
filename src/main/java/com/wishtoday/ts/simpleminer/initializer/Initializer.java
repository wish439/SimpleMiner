package com.wishtoday.ts.simpleminer.initializer;

import net.minecraft.server.MinecraftServer;

public interface Initializer {
    void initOnServerStarted(MinecraftServer server);
}
