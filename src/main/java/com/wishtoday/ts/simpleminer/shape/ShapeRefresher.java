package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ControllerManager;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.lifecycle.Lifecycle;
import com.wishtoday.ts.simpleminer.network.MineBlockSyncS2CPayload;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

@Service
public class ShapeRefresher implements Lifecycle {
    private final ControllerManager pressManager;
    private final ShapeWalker walker;
    @CreateConstruction
    public ShapeRefresher(ControllerManager pressManager, ShapeWalker walker) {
        this.pressManager = pressManager;
        this.walker = walker;
    }
    @Override
    public void onTick(MinecraftServer server) {
        pressManager.filterPressesMinerShapeInfo().forEach(info -> this.tryRefresh(info, this.pressManager.getIndividualConfig(info)));
    }

    private void tryRefresh(MinerShapeInfo info, IndividualConfig config) {
        PlayerEntity player = info.getPlayer();
        BlockPos raycast = WorldUtils.raycast(player);
        Vec3d rotationVec = player.getRotationVec(1.0f);
        Direction facing = Direction.getFacing(rotationVec);
        if (facing == null) return;
        if (raycast == null) {
            return;
        }
        if (!facing.equals(info.getCurrentDirection())) {
            this.refresh(info, raycast);
            info.setCurrentDirection(facing);
            return;
        }

        if (!raycast.equals(info.getCurrentBlockPos())) {
            this.refresh(info, raycast);
            info.setCurrentBlockPos(raycast);
        }
    }

    public void refreshForce(MinerShapeInfo info) {
        BlockPos raycast = WorldUtils.raycast(info.getPlayer());
        if (raycast == null) {
            return;
        }
        this.refresh(info, raycast);
    }

    public void refresh(MinerShapeInfo info, BlockPos raycast) {
        if (raycast == null) return;
        if (info == null) return;
        ShapeResult shapeResult = this.walker.tryWalk(info, raycast, this.pressManager.getIndividualConfig(info));
        info.setBlockPoses(shapeResult);
        LongList list = shapeResult.getSortedBlockPoses();
        Set<BlockPos> collect = list.longStream().limit(256).mapToObj(BlockPos::fromLong).collect(HashSet::new, HashSet::add, HashSet::addAll);
        ServerPlayNetworking.send((ServerPlayerEntity) info.getPlayer(), new MineBlockSyncS2CPayload(list.size(), collect));
    }
}
