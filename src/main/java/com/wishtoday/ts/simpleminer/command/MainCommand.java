package com.wishtoday.ts.simpleminer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.ReloadableReloader;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.config.OpenConfigS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Service
public class MainCommand {
    private final ReloadableReloader reloader;
    private final PressManager manager;

    public MainCommand(ReloadableReloader reloader, PressManager manager) {
        this.reloader = reloader;
        this.manager = manager;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, Function<PlayerEntity, ServerConfig> configSupplier, Function<PlayerEntity, IndividualConfig> individualConfigSupplier) {
        dispatcher.register(literal("simpleminer")
                .then(literal("config")
                        .then(literal("individual")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return -1;
                                    return executeOpenGUI(ConfigType.INDIVIDUAL, context, individualConfigSupplier.apply(player));
                                })
                                .then(literal("set")
                                        .then(literal("maxSize")
                                                .then(argument("value", IntegerArgumentType.integer(-1))
                                                        .executes(this::executeUpdateIndividualConfigMaxSize)))))
                                .then(literal("server")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            if (player == null) return -1;
                                            return executeOpenGUI(ConfigType.SERVER, context, configSupplier.apply(player));
                                        })
                                        .then(literal("reload")
                                                .executes(context -> {
                                                    this.reloader.reload();
                                                    return 1;
                                                })))
                        ));
    }

    private static int executeOpenGUI(ConfigType type, CommandContext<ServerCommandSource> context, Object supplier) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayNetworking.send(player, new OpenConfigS2CPayload(type, supplier));
        return 1;
    }

    private int executeUpdateIndividualConfigMaxSize(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return -1;
        PlayerMinerInfo info = this.manager.getPlayerMinerInfo(player);
        info.getCurrentIndividualConfig().setPersonalMaxSize(IntegerArgumentType.getInteger(context, "value"));
        return 1;
    }
}
