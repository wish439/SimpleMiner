package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.ShapeContext;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class NetworkingRegistry {
    private ServerConfig serverConfig;
    private PressManager pressManager;
    private Shapes shapes;

    @CreateConstruction
    public NetworkingRegistry(ServerConfig serverConfig, PressManager pressManager, Shapes shapes) {
        this.serverConfig = serverConfig;
        this.pressManager = pressManager;
        this.shapes = shapes;
    }

    @PostConstruct
    public void registerChannels() {
        PayloadTypeRegistry.playC2S().register(KeywordPressedPayload.ID, KeywordPressedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeywordPressedPayload.ID, this::handleKeywordPayload);
        PayloadTypeRegistry.playC2S().register(SyncConfigC2SPayload.ID, SyncConfigC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SyncConfigC2SPayload.ID, this::handleSyncConfigPayload);
    }

    private void handleSyncConfigPayload(SyncConfigC2SPayload payload, ServerPlayNetworking.Context context) {
        if (payload.type() == ConfigType.SERVER) {
            this.serverConfig.setFromConfig((ServerConfig) payload.config());
            return;
        }
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(context.player());
        info.setCurrentIndividualConfig((IndividualConfig) payload.config());
    }

    private void handleKeywordPayload(KeywordPressedPayload payload, ServerPlayNetworking.Context context) {
        pressManager.togglePlayerState(payload.press(), context.player(), payload.shapeIndex());
        if (!payload.press()) {
            return;
        }
        PlayerMinerInfo info = pressManager.getPressedPlayerMinerInfo(context.player());
        if (info == null) return;
        Shape shape = shapes.getFromIndex(info.getCurrentShape());
        ShapeContext shapeContext = this.getShapeContext(context.player(), serverConfig.getMaxSize());
        if (shapeContext == null) return;
        PlayerEntity player = info.getPlayer();
        Set<BlockPos> blockPoses = shape.walk(shapeContext);
        ArrayList<BlockPos> blockPos = new ArrayList<>(blockPoses);
        blockPos.sort((a, b) -> {
            double da = player.getBlockPos().getSquaredDistance(a);
            double db = player.getBlockPos().getSquaredDistance(b);
            return Double.compare(da, db);
        });
        info.setBlockPoses(new ShapeResult(blockPoses, blockPos));
    }

    private @Nullable ShapeContext getShapeContext(@NotNull PlayerEntity player, int maxSize) {
        World world = player.getWorld();
        BlockPos raycast = WorldUtils.raycast(player);
        if (raycast == null) {
            return null;
        }
        return new ShapeContext(maxSize, player, raycast, world.getBlockState(raycast), world);
    }
}
