package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.command.MainCommand;
import com.wishtoday.ts.simpleminer.command.TestCommands;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

@Service
public class EventRegistry {
    private final PressManager manager;
    private final ServerConfig serverConfig;
    private final ShapeRefresher shapeRefresher;
    private final BlockBreaker blockBreaker;
    private final TestCommands testCommands;
    private final MainCommand mainCommand;

    @CreateConstruction
    public EventRegistry(PressManager manager, ServerConfig serverConfig, BlockBreaker blockBreaker, ShapeRefresher shapeRefresher, TestCommands testCommands, MainCommand mainCommand) {
        this.manager = manager;
        this.serverConfig = serverConfig;
        this.blockBreaker = blockBreaker;
        this.shapeRefresher = shapeRefresher;
        this.testCommands = testCommands;
        this.mainCommand = mainCommand;
    }

    @PostConstruct
    public void registerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(this.shapeRefresher::onTick);
        PlayerBlockBreakEvents.BEFORE.register(this.blockBreaker::breakBlock);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, registrationEnvironment) -> {
            mainCommand.register(dispatcher, player -> serverConfig, player -> this.manager.getPlayerMinerInfo(player).getCurrentIndividualConfig());
            this.testCommands.register(dispatcher);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            this.manager.togglePlayerState(false, player, 0);
        });
    }
}
