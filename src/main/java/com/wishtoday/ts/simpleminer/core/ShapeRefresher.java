package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.ShapeContext;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Service
public class ShapeRefresher {
    private final ServerConfig serverConfig;
    private final PressManager pressManager;
    private final Shapes shapes;
    @CreateConstruction
    public ShapeRefresher(ServerConfig serverConfig, PressManager pressManager, Shapes shapes) {
        this.serverConfig = serverConfig;
        this.pressManager = pressManager;
        this.shapes = shapes;
    }
    public void onTick(MinecraftServer server) {
        pressManager.filterPressesPlayerInfos().forEach(this::tryRefresh);
    }

    private void tryRefresh(PlayerMinerInfo info) {
        BlockPos raycast = WorldUtils.raycast(info.getPlayer());
        if (raycast == null) {
            return;
        }
        if (info.getCurrentBlockPos() == null) {
            this.refresh(info, raycast);
            info.setCurrentBlockPos(raycast);
            return;
        }

        if (!raycast.equals(info.getCurrentBlockPos())) {
            this.refresh(info, raycast);
            info.setCurrentBlockPos(raycast);
        }
    }

    public void refresh(PlayerMinerInfo info, BlockPos raycast) {
        if (info == null) return;
        PlayerEntity player = info.getPlayer();
        Shape shape = shapes.getFromIndex(info.getCurrentShape());
        int maxSize = serverConfig.getMaxSize();
        int personalMaxSize = info.getCurrentIndividualConfig().getPersonalMaxSize();
        int i = personalMaxSize == -1 ? maxSize : Math.min(maxSize, personalMaxSize);

        ShapeContext shapeContext = this.getShapeContext(player, i, raycast);

        LongOpenHashSet blockPoses = shape.walk(shapeContext);
        LongArrayList blockPos = new LongArrayList(blockPoses);
        blockPos.sort((a, b) -> {
            BlockPos pos = BlockPos.fromLong(a);
            BlockPos pos2 = BlockPos.fromLong(b);
            double da = player.getBlockPos().getSquaredDistance(pos);
            double db = player.getBlockPos().getSquaredDistance(pos2);
            return Double.compare(da, db);
        });
        info.setBlockPoses(new ShapeResult(blockPoses, blockPos));
    }

    private @NotNull ShapeContext getShapeContext(@NotNull PlayerEntity player, int maxSize, BlockPos raycast) {
        World world = player.getWorld();
        return new ShapeContext(maxSize, player, raycast, world.getBlockState(raycast), world);
    }
}
