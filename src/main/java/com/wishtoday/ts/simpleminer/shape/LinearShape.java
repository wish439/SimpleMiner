package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.utils.BlockSorter;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.function.Predicate;

@Service
public class LinearShape implements Shape {
    private final PressManager manager;

    @CreateConstruction
    public LinearShape(PressManager manager) {
        this.manager = manager;
    }

    @Override
    public LongOpenHashSet walk(ShapeContext context) {
        Direction facing = context.getDirection();
        BlockPos currentTargetPos = context.getCurrentTargetPos();
        PlayerEntity player = context.getPlayer();
        long targetPosLong = currentTargetPos.asLong();
        PlayerMinerInfo info = manager.getPlayerMinerInfo(player);
        World world = context.getWorld();
        int maxSize = context.getMaxSize();
        LongOpenHashSet longArrayList = this.generateLinearFromInfos(info.getLinearShapeInfos().getWidth(), info.getLinearShapeInfos().getHeight(), BlockPos.fromLong(targetPosLong), facing, p -> {
            //BlockState blockState = world.getBlockState(p);
            //return !blockState.isAir();
            return true;
        });

        int currentStep = 0;

        LongOpenHashSet longs = LongOpenHashSet.of(targetPosLong);
        LongOpenHashSet lastLongs = new LongOpenHashSet();
        boolean willEnd;
        OUTLINE:
        while (maxSize > longs.size()) {
            LongIterator iterator = longArrayList.iterator();
            int count = 0;
            int i = longs.size() + longArrayList.size();
            willEnd = i >= maxSize;
            while (iterator.hasNext()) {
                //if (count == longArrayList.size()) break OUTLINE;
                //if (skipIndex.size() == longArrayList.size()) break OUTLINE;
                long l = iterator.nextLong();
                //if (skipIndex.contains(l)) continue;
                long add = BlockPos.add(l, facing.getOffsetX() * currentStep, facing.getOffsetY() * currentStep, facing.getOffsetZ() * currentStep);
                BlockState state = ((WorldExtension) world).simpleMiner$getBlockState(add);
                if (state.isAir()) {
                    //skipIndex.add(l);
                    count++;
                    if (count == longArrayList.size()) break OUTLINE;
                    continue;
                }
                if (willEnd) {
                    lastLongs.add(add);
                    continue;
                }
                if (maxSize <= longs.size()) break OUTLINE;
                longs.add(add);
            }
            LongArrayList list = BlockSorter.sortWithPlayerManhattan(lastLongs, player);
            int min = Math.min(maxSize - longs.size(), list.size());
            LongList longList = list.subList(0, min);
            longs.addAll(longList);
            lastLongs.clear();
            currentStep++;
        }
        return longs;
    }

    private LongOpenHashSet generateLinearFromInfos(
            int width
            , int height
            , BlockPos initial
            , Direction currentFacing
            , Predicate<BlockPos> filter) {
        Direction.Axis[] axes = this.getDirections(currentFacing);
        height -= 1;
        width -= 1;
        double ceil = Math.ceil((double) width / 2);
        double remainWidth = width - ceil;
        BlockPos offset = initial.offset(axes[0], (int) ceil);
        BlockPos remainBlockPos = initial.offset(axes[0], -(int) remainWidth);
        double ceilHeight = Math.ceil((double) height / 2);
        double remainHeight = height - ceilHeight;
        BlockPos offsetHeight = initial.offset(axes[1], (int) ceilHeight);
        BlockPos remainHeightBlockPos = initial.offset(axes[1], -(int) remainHeight);
        BlockPos pos = new BlockPos(Math.max(offset.getX(), offsetHeight.getX()), Math.max(offset.getY(), offsetHeight.getY()), Math.max(offset.getZ(), offsetHeight.getZ()));
        BlockPos pos2 = new BlockPos(Math.min(remainBlockPos.getX(), remainHeightBlockPos.getX()), Math.min(remainBlockPos.getY(), remainHeightBlockPos.getY()), Math.min(remainBlockPos.getZ(), remainHeightBlockPos.getZ()));
        LongOpenHashSet result = new LongOpenHashSet();
        Iterable<BlockPos> iterate = BlockPos.iterate(pos, pos2);
        iterate.forEach(blockPos -> {
            if (filter.test(blockPos)) {
                result.add(blockPos.asLong());
            }
        });
        return result;
    }

    private Direction.Axis[] getDirections(Direction facing) {
        if (facing.getAxis() == Direction.Axis.Y) {
            return new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z};
        }
        if (facing.getAxis() == Direction.Axis.Z) {
            return new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y};
        }
        return new Direction.Axis[]{Direction.Axis.Z, Direction.Axis.Y};
    }

    @Override
    public int index() {
        return 1;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("线形");
    }
}
