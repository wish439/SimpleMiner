package com.wishtoday.ts.simpleminer.services;

import com.wishtoday.simpleservices.services.LoadCondition;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class ClientOnlyLoadCondition implements LoadCondition {
    @Override
    public boolean allowLoad() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
}
