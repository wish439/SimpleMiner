package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.network.MineBlockSyncS2CPayload;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
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
public class ShapeRefresher {
    private final PressManager pressManager;
    private final ShapeWalker walker;
    @CreateConstruction
    public ShapeRefresher(PressManager pressManager, ShapeWalker walker) {
        this.pressManager = pressManager;
        this.walker = walker;
    }
    public void onTick(MinecraftServer server) {
        pressManager.filterPressesPlayerInfos().forEach(this::tryRefresh);
    }

    private void tryRefresh(PlayerMinerInfo info) {
        PlayerEntity player = info.getPlayer();
        BlockPos raycast = WorldUtils.raycast(player);
        Vec3d rotationVec = player.getRotationVec(1.0f);
        Direction facing = Direction.getFacing(rotationVec);
        if (facing == null) return;
        if (raycast == null) {
            return;
        }
        if (info.getCurrentDirection() == null) {
            this.refresh(info, raycast);
            info.setCurrentDirection(facing);
            return;
        }
        if (!facing.equals(info.getCurrentDirection())) {
            this.refresh(info, raycast);
            info.setCurrentDirection(facing);
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
        ShapeResult shapeResult = this.walker.tryWalk(info, raycast);
        info.setBlockPoses(shapeResult);
        LongList list = shapeResult.getSortedBlockPoses();
        Set<BlockPos> collect = list.longStream().limit(256).mapToObj(BlockPos::fromLong).collect(HashSet::new, HashSet::add, HashSet::addAll);
        ServerPlayNetworking.send((ServerPlayerEntity) info.getPlayer(), new MineBlockSyncS2CPayload(list.size(), collect));
    }
}
