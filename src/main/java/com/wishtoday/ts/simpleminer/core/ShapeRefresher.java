package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.matcher.BlockMatcher;
import com.wishtoday.ts.simpleminer.network.MineBlockSyncS2CPayload;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.ShapeContext;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import com.wishtoday.ts.simpleminer.utils.BlockSorter;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Service
public class ShapeRefresher {
    private final ServerConfig serverConfig;
    private final PressManager pressManager;
    private final Shapes shapes;
    private final BlockMatcher matcher;
    @CreateConstruction
    public ShapeRefresher(ServerConfig serverConfig, PressManager pressManager, Shapes shapes, BlockMatcher matcher) {
        this.serverConfig = serverConfig;
        this.pressManager = pressManager;
        this.shapes = shapes;
        this.matcher = matcher;
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

    public void refreshForce(PlayerMinerInfo info) {
        BlockPos raycast = WorldUtils.raycast(info.getPlayer());
        if (raycast == null) {
            return;
        }
        this.refresh(info, raycast);
    }

    public void refresh(PlayerMinerInfo info, BlockPos raycast) {
        if (raycast == null) return;
        if (info == null) return;
        PlayerEntity player = info.getPlayer();
        Shape shape = shapes.getFromIndex(info.getCurrentShape());
        int maxSize = serverConfig.getMaxSize();
        int personalMaxSize = info.getCurrentIndividualConfig().getPersonalMaxSize();
        int i = personalMaxSize == -1 ? maxSize : Math.min(maxSize, personalMaxSize);

        ShapeContext shapeContext = this.getShapeContext(player, i, raycast, info);

        LongOpenHashSet blockPoses = shape.walk(shapeContext);
        LongArrayList blockPos = new LongArrayList(blockPoses);
        LongArrayList list = BlockSorter.sortWithPlayerManhattan(blockPos, player);
        info.setBlockPoses(new ShapeResult(blockPoses, list));
        ServerPlayNetworking.send((ServerPlayerEntity) player, new MineBlockSyncS2CPayload(list.size()));
    }

    private @NotNull ShapeContext getShapeContext(@NotNull PlayerEntity player, int maxSize, BlockPos raycast, PlayerMinerInfo info) {
        World world = player.getWorld();
        Vec3d rotationVec = player.getRotationVec(1.0f);
        Direction facing = Direction.getFacing(rotationVec);
        return new ShapeContext(maxSize, player, raycast, world.getBlockState(raycast), world, facing, this.matcher, info.getCurrentIndividualConfig());
    }
}
