package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.BlockBreaker;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

@Service
public class EventRegistry {

    private final ShapeRefresher shapeRefresher;
    private final BlockBreaker blockBreaker;

    @CreateConstruction
    public EventRegistry(BlockBreaker blockBreaker, ShapeRefresher shapeRefresher) {
        this.blockBreaker = blockBreaker;
        this.shapeRefresher = shapeRefresher;
    }

    @PostConstruct
    public void registerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(this.shapeRefresher::onTick);
        PlayerBlockBreakEvents.BEFORE.register(this.blockBreaker::breakBlock);
    }
}
