package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.command.MainCommand;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.core.rightClick.MinerRightHandler;
import com.wishtoday.ts.simpleminer.io.PersistenceService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

@Service
public class EventRegistry {
    private final PressManager manager;
    private final ServerConfig serverConfig;
    private final ShapeRefresher shapeRefresher;
    private final BlockBreaker blockBreaker;
    private final MinerRightHandler rightHandler;
    private final MainCommand mainCommand;
    private final PersistenceService persistence;

    @CreateConstruction
    public EventRegistry(PressManager manager, ServerConfig serverConfig, BlockBreaker blockBreaker, ShapeRefresher shapeRefresher, MinerRightHandler rightHandler, MainCommand mainCommand, PersistenceService persistence) {
        this.manager = manager;
        this.serverConfig = serverConfig;
        this.blockBreaker = blockBreaker;
        this.shapeRefresher = shapeRefresher;
        this.rightHandler = rightHandler;
        this.mainCommand = mainCommand;
        this.persistence = persistence;
    }

    @PostConstruct
    public void registerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(this.shapeRefresher::onTick);
        PlayerBlockBreakEvents.BEFORE.register(this.blockBreaker::breakBlock);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, registrationEnvironment) -> {
            mainCommand.register(dispatcher, player -> serverConfig, player -> this.manager.getPlayerMinerInfo(player).getCurrentIndividualConfig());
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            this.manager.togglePlayerState(false, player, 0);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            ServerPlayerEntity player = handler.getPlayer();
            this.persistence.savePlayerData(player);
            this.manager.removePlayerMinerInfo(player.getUuid());
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            return this.rightHandler.handleRightClick((ServerPlayerEntity) player, world, hand, hitResult);
        });
    }
}
