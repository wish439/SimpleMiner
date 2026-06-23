package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
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

import java.util.List;
import java.util.Set;

@Service
public class NetworkingRegistry {
    private PressManager pressManager;
    private Shapes shapes;

    @CreateConstruction
    public NetworkingRegistry(PressManager pressManager, Shapes shapes) {
        this.pressManager = pressManager;
        this.shapes = shapes;
    }

    @PostConstruct
    public void registerChannels() {
        PayloadTypeRegistry.playC2S().register(KeywordPressedPayload.ID, KeywordPressedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeywordPressedPayload.ID, this::handleKeywordPayload);
    }

    private void handleKeywordPayload(KeywordPressedPayload payload, ServerPlayNetworking.Context context) {
        pressManager.togglePlayerState(payload.press(), context.player(), payload.shapeIndex());
        if (!payload.press()) {
            return;
        }
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(context.player());
        //if (info == null) return;
        Shape shape = shapes.getFromIndex(info.getCurrentShape());
        ShapeContext shapeContext = this.getShapeContext(context.player(), 100 * 500);
        if (shapeContext == null) return;
        Set<BlockPos> blockPoses = shape.walk(shapeContext);
        info.setBlockPoses(blockPoses);
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
