package com.wishtoday.ts.simpleminer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.config.OpenConfigS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.literal;

public class MainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, Function<PlayerEntity, ServerConfig> configSupplier, Function<PlayerEntity, IndividualConfig> individualConfigSupplier) {
        dispatcher.register(literal("simpleminer")
                .then(literal("config")
                        .then(literal("individual")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return -1;
                                    return executeOpenGUI(ConfigType.INDIVIDUAL, context, individualConfigSupplier.apply(player));
                                }))
                        .then(literal("server")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) return -1;
                                    return executeOpenGUI(ConfigType.SERVER, context, configSupplier.apply(player));
                                })))
        );
    }

    private static int executeOpenGUI(ConfigType type, CommandContext<ServerCommandSource> context, Object supplier) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayNetworking.send(player, new OpenConfigS2CPayload(type, supplier));
        return 1;
    }
}
