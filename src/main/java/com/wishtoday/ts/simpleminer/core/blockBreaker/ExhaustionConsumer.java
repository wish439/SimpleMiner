package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import net.minecraft.entity.player.PlayerEntity;

@Service
public class ExhaustionConsumer implements Reloadable {
    private float exhaustionPerBlocks;

    @CreateConstruction
    public ExhaustionConsumer(ServerConfig config) {
        this.reload(config);
    }

    public void consume(PlayerEntity player) {
        player.addExhaustion(this.exhaustionPerBlocks);
    }

    @Override
    public boolean reload(ServerConfig config) {
        this.exhaustionPerBlocks = config.getExhaustionPerBlocks();
        return true;
    }
}
