package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.ShapeContext;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Service
public class ShapeRefresher {
    private final PressManager pressManager;
    private final Shapes shapes;
    @CreateConstruction
    public ShapeRefresher(PressManager pressManager,  Shapes shapes) {
        this.pressManager = pressManager;
        this.shapes = shapes;
    }
    public void onTick(MinecraftServer server) {
        pressManager.getPlayerMinerInfos().forEach(this::tryRefresh);
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

    private void refresh(PlayerMinerInfo info, BlockPos raycast) {
        if (info == null) return;
        Shape shape = shapes.getFromIndex(info.getCurrentShape());
        ShapeContext shapeContext = this.getShapeContext(info.getPlayer(), 100 * 500, raycast);

        Set<BlockPos> blockPoses = shape.walk(shapeContext);
        info.setBlockPoses(blockPoses);
    }

    private @NotNull ShapeContext getShapeContext(@NotNull PlayerEntity player, int maxSize, BlockPos raycast) {
        World world = player.getWorld();
        return new ShapeContext(maxSize, player, raycast, world.getBlockState(raycast), world);
    }
}
