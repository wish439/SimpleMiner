package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.core.blockBreaker.*;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ItemCollectorRouter;
import com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker.SingleBlockBreakerRouter;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.debug.NanoTimerAccumulator;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.schedule.Task;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class OnceMiningTask implements Task {
    private final MinerShapeInfo info;
    private final IndividualConfig config;
    private final ShapeResult shapeResult;
    private final ShapeAnalyzer shapeAnalyzer;
    private final List<BlockBreakerFeature> features;
    private final ItemCollectorRouter collector;
    private final ItemDropper dropper;
    private final SingleBlockBreakerRouter blockBreaker;
    private final ExhaustionConsumer exhaustionConsumer;
    private final PlayerEntity player;
    private final World world;
    private final BlockPos initialPos;
    private final BlockState initialState;
    private final BlockEntity blockEntity;
    private static final ThreadLocal<Boolean> blockBreaking = ThreadLocal.withInitial(() -> false);

    public static boolean isBlockBreaking() {
        return blockBreaking.get();
    }

    //This should be removed.
    private static final boolean DEBUG_MODE = true;

    public OnceMiningTask(MinerShapeInfo info
            , IndividualConfig config
            , ShapeResult shapeResult
            , List<BlockBreakerFeature> features
            , ShapeAnalyzer shapeAnalyzer
            , ItemCollectorRouter collector
            , ItemDropper dropper
            , SingleBlockBreakerRouter blockBreaker
            , ExhaustionConsumer exhaustionConsumer
            , PlayerEntity player
            , World world
            , BlockPos initialPos
            , BlockState initialState
            , BlockEntity blockEntity
    ) {
        this.info = info;
        this.config = config;
        this.shapeResult = shapeResult;
        this.shapeAnalyzer = shapeAnalyzer;
        this.features = features;
        this.collector = collector;
        this.dropper = dropper;
        this.blockBreaker = blockBreaker;
        this.exhaustionConsumer = exhaustionConsumer;
        this.player = player;
        this.world = world;
        this.initialPos = initialPos;
        this.initialState = initialState;
        this.blockEntity = blockEntity;
    }

    @Override
    public void run() {
        if (blockBreaking.get()) {
            return;
        }
        ItemStack mainHandStack = player.getMainHandStack();
        if (shapeResult == null) return;
        long a = System.nanoTime();
        LongList sortedBlockPoses = shapeResult.getSortedBlockPoses();
        LongOpenHashSet internal = this.blockBreaker.canControlUpdate() ? shapeAnalyzer.calcCompleteSurrounded(shapeResult.getBlockPoses()) : new LongOpenHashSet();
        BlockBreakContext context = new BlockBreakContext(world, player, initialPos, this.initialState, blockEntity, info, config, shapeResult, mainHandStack);


        NanoTimerAccumulator setBlock = new NanoTimerAccumulator();
        NanoTimerAccumulator removeBlockEntity = new NanoTimerAccumulator();
        NanoTimerAccumulator blockAdded = new NanoTimerAccumulator();
        NanoTimerAccumulator all = new NanoTimerAccumulator();
        NanoTimerAccumulator set = new NanoTimerAccumulator();

        if (DEBUG_MODE) {
            System.out.println("internal consuming" + (System.nanoTime() - a));
        }

        this.forEachFeatures(b -> b.beforeBreakCandidate(context));

        this.collector.start();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        CollectContext collectContext = new CollectContext(world, player, null, mainHandStack, null, null, false);
        ServerWorld serverWorld = (ServerWorld) world;
        long d = 0;
        long c = 0;
        MAINCYCLE:
        for (long blockPose : sortedBlockPoses) {
            mutable.set(blockPose);
            context.setCurrentPos(mutable);
            BlockState currentState = ((WorldExtension) world).simpleMiner$getBlockState(blockPose);
            context.setCurrentState(currentState);
            collectContext.setBlockState(currentState);

            this.forEachFeatures(b -> b.beforeBlockBreak(context));

            collectContext.setPos(mutable);
            boolean canHarvest = player.canHarvest(currentState);
            collectContext.setCanHarvest(canHarvest);
            boolean collectItem = true;
            boolean collectExperience = true;
            for (BlockBreakerFeature feature : this.features) {
                if (!feature.allowCollectItem(context, collectContext)) {
                    collectItem = false;
                    break;
                }
            }
            for (BlockBreakerFeature feature : this.features) {
                if (!feature.allowCollectExperience(context, collectContext)) {
                    collectExperience = false;
                    break;
                }
            }

            if (this.collector.shouldCollect(collectContext) && collectItem) {
                long _s = System.nanoTime();
                this.collector.collectItem(collectContext);
                if (collectExperience)
                    this.collector.collectExperience(collectContext);
                c += System.nanoTime() - _s;
            }

            boolean breakBlock = true;

            for (BlockBreakerFeature feature : this.features) {
                if (!feature.allowBreak(context)) {
                    breakBlock = false;
                }
            }

            boolean empty = mainHandStack.isEmpty();

            if (breakBlock) {
                long _s = System.nanoTime();
                boolean b;
                if (DEBUG_MODE) {
                    b = this.breakBlockDebug(mutable, currentState, world, player, mainHandStack, !internal.contains(blockPose), canHarvest, setBlock, removeBlockEntity, blockAdded, all, set);
                } else b = this.breakBlock(mutable, currentState, world, player, mainHandStack, !internal.contains(blockPose), canHarvest);
                if (canHarvest && b) {
                    this.exhaustionConsumer.consume(player);
                }
                d += System.nanoTime() - _s;
            }


            if (!empty && player.getMainHandStack().isEmpty()) {
                break;
            }

            for (BlockBreakerFeature feature : this.features) {
                if (!feature.afterBlockBreakAllowContinue(context)) {
                    break MAINCYCLE;
                }
            }
        }
        if (DEBUG_MODE) {
            System.out.println("breaking " + d);
            System.out.println("collecting " + c);

            System.out.println("setBlock: " + setBlock);
            System.out.println("removeBlockEntity: " + removeBlockEntity);
            System.out.println("blockAdded: " + blockAdded);

            System.out.println("all: " + all);
            System.out.println("set: " + set);
            System.out.println("all - set: " + (all.getAccumulated() - set.getAccumulated()));
        }
        CollectedResult result = this.collector.finish();
        List<ItemStack> droppedStacks = this.dropper.dropStack(context.getWorld(), context.getOriginPos(), result.getMap());

        this.forEachFeatures(b -> b.afterBrokenCandidate(context, droppedStacks, result.getMap()));

        if (result.getExperience() <= 0) {
            return;
        }
        ExperienceOrbEntity experienceOrbEntity = new ExperienceOrbEntity(world, initialPos.getX() + 0.5D, initialPos.getY() + 0.5D, initialPos.getZ() + 0.5D, result.getExperience());
        (serverWorld).spawnEntityAndPassengers(experienceOrbEntity);
    }

    private void forEachFeatures(Consumer<BlockBreakerFeature> consumer) {
        if (features.isEmpty()) return;
        this.features.forEach(consumer);
    }

    private boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest) {
        try {
            blockBreaking.set(true);
            return this.blockBreaker.breakBlock(pos, state, world, player, mainHandStack, update, canHarvest);
        } finally {
            blockBreaking.set(false);
        }
    }

    private boolean breakBlockDebug(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest, NanoTimerAccumulator setBlock, NanoTimerAccumulator removeBlockEntity, NanoTimerAccumulator blockAdded, NanoTimerAccumulator all, NanoTimerAccumulator set) {
        try {
            blockBreaking.set(true);
            return this.blockBreaker.breakBlockDebug(pos, state, world, player, mainHandStack, update, canHarvest, setBlock, removeBlockEntity, blockAdded, all, set);
        } finally {
            blockBreaking.set(false);
        }
    }
}
