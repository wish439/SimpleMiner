package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ControllerManager;
import com.wishtoday.ts.simpleminer.GlobalCondition;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.core.FragmentMiningTask;
import com.wishtoday.ts.simpleminer.core.OnceMiningTask;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ExternalStorageAdvocatedCollectorRouter;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.core.ShapeAnalyzer;
import com.wishtoday.ts.simpleminer.schedule.TickScheduler;
import com.wishtoday.ts.simpleminer.shape.ShapeRefresher;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ItemCollectorRouter;
import com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker.SingleBlockBreakerRouter;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import it.unimi.dsi.fastutil.longs.*;
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
//Actually my imagination should attempt in another block breaker impl.

//In my imagination, if we want faster, we should be like
//1.Block breaking use palette.
//2.S2C updata packet should be a batch operation instead of every block one update packet.
//eeee, this has already been done in vanilla.
//3.This is first opinion's side effect: waiting palette completed, after do those side effects.




//breaking: TEST-----collector: SAMPLE in 60,000, nano time
//internal consuming4800
//breaking 70280500
//collecting 7895300
//--------------------------------
//after removed heightmap
//internal consuming2200
//breaking 57680100
//collecting 6593200
//------------------------------------------------------
//after removed paletted swap--five
//internal consuming1200
//breaking 50671800
//collecting 7298800

//internal consuming1300
//breaking 49767300
//collecting 7317600

//internal consuming1300
//breaking 43299500
//collecting 6330600

//internal consuming1500
//breaking 46565400
//collecting 6802000

//internal consuming2000
//breaking 46720300
//collecting 6828200

//------------------------------------

//breaking: PUREAPI-----collector: PUREAPI in 60,000, nano time
//internal consuming13569900
//breaking 204292100
//collecting 52875800

//breaking: VANINNA-----collector: INTERCEPT(default config) in 60,000, nano time
//A:gamemode: Creative
//internal consuming39500
//breaking 235838600
//collecting 0

//B:gamemode: survival
//internal consuming3000
//breaking 532839700
//collecting 0 -----bug, can't be counted properly

@Service
public class BlockBreaker {
    private final ControllerManager pressManager;
    private final ShapeRefresher shapeRefresher;
    private final ShapeAnalyzer shapeAnalyzer;
    private final List<BlockBreakerFeature> features;
    private final ItemCollectorRouter collector;
    private final ItemDropper dropper;
    private final SingleBlockBreakerRouter blockBreaker;
    private final ExhaustionConsumer exhaustionConsumer;
    private final TickScheduler scheduler;
    private final ExternalStorageAdvocatedCollectorRouter externalStorageAdvocatedCollectorRouter;
    private static final ThreadLocal<Boolean> blockBreaking = ThreadLocal.withInitial(() -> false);


    //TODO: remove in publish version
    private static final boolean DEBUG_MODE = true;

    @CreateConstruction
    public BlockBreaker(ControllerManager pressManager
            , ShapeRefresher shapeRefresher
            , List<BlockBreakerFeature> features
            , ShapeAnalyzer shapeAnalyzer
            , ItemCollectorRouter collector
            , ItemDropper dropper, SingleBlockBreakerRouter blockBreaker, ExhaustionConsumer exhaustionConsumer, TickScheduler scheduler, ExternalStorageAdvocatedCollectorRouter externalStorageAdvocatedCollectorRouter
    ) {
        this.pressManager = pressManager;
        this.shapeRefresher = shapeRefresher;
        this.shapeAnalyzer = shapeAnalyzer;
        this.features = features;
        this.collector = collector;
        this.dropper = dropper;
        this.blockBreaker = blockBreaker;
        this.exhaustionConsumer = exhaustionConsumer;
        this.scheduler = scheduler;
        this.externalStorageAdvocatedCollectorRouter = externalStorageAdvocatedCollectorRouter;
    }

    public static boolean getBlockBreaking() {
        return blockBreaking.get();
    }

    //TODO:Try to parallel the work of the analyzer with the block breaking。
    //TODO:set the default update strength of the block breaking to false, and complete other updates(side effects) after the analyzer completes
    public boolean breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (GlobalCondition.isBlockBreaking()) {
            return true;
        }
        ItemStack mainHandStack = player.getMainHandStack();
        MinerShapeInfo info = this.pressManager.getPressedMinerShapeInfo(player);
        if (info == null) return true;
        IndividualConfig config = this.pressManager.getIndividualConfig(info);
        this.shapeRefresher.refresh(info, pos);
        ShapeResult shapeResult = info.getBlockPoses();
        if (shapeResult == null) return true;
        BlockBreakContext context = new BlockBreakContext(world, player, pos, state, blockEntity, info, config, shapeResult, mainHandStack);
        if (false) {
            FragmentMiningTask task = new FragmentMiningTask(externalStorageAdvocatedCollectorRouter, dropper, blockBreaker, exhaustionConsumer, player, world, pos, shapeResult.getSortedBlockPoses(), 10 * 1000, 0, scheduler, features, context);
            this.scheduler.scheduleCurrentTick(task);
            return false;
        }
        //Test
        if (true) {
            this.scheduler.scheduleCurrentTick(new OnceMiningTask(
                    info, config, shapeResult
                    , features, shapeAnalyzer, collector
                    , dropper, blockBreaker
                    , exhaustionConsumer, player, world
                    , pos, state, blockEntity));
            return false;
        }
        long a = System.nanoTime();
        LongList sortedBlockPoses = shapeResult.getSortedBlockPoses();
        LongOpenHashSet internal = this.blockBreaker.canControlUpdate() ? shapeAnalyzer.calcCompleteSurrounded(shapeResult.getBlockPoses()) : new LongOpenHashSet();
        //BlockBreakContext context = new BlockBreakContext(world, player, pos, state, blockEntity, info, config, shapeResult, mainHandStack);

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
                boolean b = this.breakBlock(mutable, currentState, world, player, mainHandStack, !internal.contains(blockPose), canHarvest);
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
        }
        CollectedResult result = this.collector.finish();
        List<ItemStack> droppedStacks = this.dropper.dropStack(context.getWorld(), context.getOriginPos(), result.getMap());

        this.forEachFeatures(b -> b.afterBrokenCandidate(context, droppedStacks, result.getMap()));

        if (result.getExperience() <= 0) {
            return false;
        }
        ExperienceOrbEntity experienceOrbEntity = new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, result.getExperience());
        (serverWorld).spawnEntityAndPassengers(experienceOrbEntity);
        return false;
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
}
