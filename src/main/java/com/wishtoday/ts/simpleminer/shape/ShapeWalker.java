package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.core.blockBreaker.LazySectionBlockMappings;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.matcher.BlockMatcher;
import com.wishtoday.ts.simpleminer.utils.BlockSorter;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@Service
public class ShapeWalker {
    private final Shapes shapes;
    private final ServerConfig serverConfig;
    private final BlockMatcher matcher;

    @CreateConstruction
    public ShapeWalker(Shapes shapes, ServerConfig serverConfig, BlockMatcher blockMatcher) {
        this.shapes = shapes;
        this.serverConfig = serverConfig;
        this.matcher = blockMatcher;
    }

    public ShapeResult tryWalk(MinerShapeInfo info, BlockPos raycast, IndividualConfig config) {
        PlayerEntity player = info.getPlayer();
        Shape shape = shapes.getFromIndex(info.getCurrentShape());
        int maxSize = serverConfig.getMaxSize();
        int personalMaxSize = config.getPersonalMaxSize();
        int i = personalMaxSize == -1 ? maxSize : Math.min(maxSize, personalMaxSize);

        ShapeContext shapeContext = this.getShapeContext(player, i, raycast, info, config);

        LongOpenHashSet blockPoses = shape.walk(shapeContext);
        return this.sortFromSet(blockPoses, player, i);
    }

    private ShapeResult sortFromSet(LongOpenHashSet blockPoses, PlayerEntity player, int maxSize) {
        LongArrayList blockPos = new LongArrayList(blockPoses);
        LongList list = BlockSorter.sortWithPlayerManhattan(blockPos, player);
        if (list.size() > maxSize) {
            list = list.subList(0, maxSize);
        }
        return new ShapeResult(blockPoses, list
                //currently.
                , new LazySectionBlockMappings(list));
    }

    private @NotNull ShapeContext getShapeContext(@NotNull PlayerEntity player, int maxSize, BlockPos raycast, MinerShapeInfo info, IndividualConfig config) {
        World world = player.getWorld();
        return new ShapeContext(maxSize, player, raycast, world.getBlockState(raycast), world, info.getCurrentDirection(), this.matcher, config);
    }
}
