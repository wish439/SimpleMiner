package com.wishtoday.ts.simpleminer.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ClientNetworkExtendFutures {
    void initialize();
}
