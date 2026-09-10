package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.core.ShapeAnalyzer;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ItemCollectorRouter;
import com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker.SingleBlockBreakerRouter;
import com.wishtoday.ts.simpleminer.mixin.Accessor.ExperienceDroppingBlockAccessor;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
//Actually my imagination should attempt in another block breaker impl.

//In my imagination, if we want faster, we should be like
//1.Block breaking use palette.
//2.S2C updata packet should be a batch operation instead of every block one update packet.
//3.This is first opinion's side effect: waiting palette completed, after do those side effects.
@Service
public class BlockBreaker {
    private final PressManager pressManager;
    private final ShapeRefresher shapeRefresher;
    private final ShapeAnalyzer shapeAnalyzer;
    private final List<BlockBreakerFeature> features;
    private final ItemCollectorRouter collector;
    private final ItemDropper dropper;
    private final SingleBlockBreakerRouter blockBreaker;
    private final ExhaustionConsumer exhaustionConsumer;
    private static final ThreadLocal<Boolean> blockBreaking = ThreadLocal.withInitial(() -> false);

    @CreateConstruction
    public BlockBreaker(PressManager pressManager
            , ShapeRefresher shapeRefresher
            , List<BlockBreakerFeature> features
            , ShapeAnalyzer shapeAnalyzer
            , ItemCollectorRouter collector
            , ItemDropper dropper, SingleBlockBreakerRouter blockBreaker, ExhaustionConsumer exhaustionConsumer
    ) {
        this.pressManager = pressManager;
        this.shapeRefresher = shapeRefresher;
        this.shapeAnalyzer = shapeAnalyzer;
        this.features = features;
        this.collector = collector;
        this.dropper = dropper;
        this.blockBreaker = blockBreaker;
        this.exhaustionConsumer = exhaustionConsumer;
    }

    public static boolean getBlockBreaking() {
        return blockBreaking.get();
    }

    //TODO:Try to parallel the work of the analyzer with the block breaking。
    //TODO:set the default update strength of the block breaking to false, and complete other updates(side effects) after the analyzer completes
    public boolean breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockBreaking.get()) {
            return true;
        }
        ItemStack mainHandStack = player.getMainHandStack();
        PlayerMinerInfo info = this.pressManager.getPressedPlayerMinerInfo(player);
        if (info == null) return true;
        this.shapeRefresher.refresh(info, pos);
        ShapeResult shapeResult = info.getBlockPoses();
        if (shapeResult == null) return true;
        LongList sortedBlockPoses = shapeResult.getSortedBlockPoses();
        LongOpenHashSet internal = shapeAnalyzer.calcCompleteSurrounded(shapeResult.getBlockPoses());
        BlockBreakContext context = new BlockBreakContext(world, player, pos, state, blockEntity, info, shapeResult, mainHandStack);

        this.forEachFeatures(b -> b.beforeCycle(context));

        this.collector.start();
        int experience = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        //Long2ObjectLinkedOpenHashMap<BlockStorage> linkedOpenHashMap = new Long2ObjectLinkedOpenHashMap<>();
        CollectContext collectContext = new CollectContext(world, player, null, mainHandStack, null, null, false);
        ServerWorld serverWorld = (ServerWorld) world;
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
            for (BlockBreakerFeature feature : this.features) {
                if (!feature.allowCollectItem(context, collectContext)) {
                    collectItem = false;
                    break;
                }
            }

            if (this.collector.shouldCollectItem(collectContext) && collectItem) {
                this.collector.collectItem(collectContext);
            }

            Block block = state.getBlock();
            int base;
            if (block instanceof ExperienceDroppingBlock e) {
                ExperienceDroppingBlockAccessor accessor = (ExperienceDroppingBlockAccessor) e;
                IntProvider experienceDropped = accessor.getExperienceDropped();
                base = EnchantmentHelper.getBlockExperience(serverWorld, mainHandStack, experienceDropped.get(serverWorld.getRandom()));
            } else {
                base = 0;
            }

            experience += EnchantmentHelper.getBlockExperience(serverWorld, mainHandStack, base);

            boolean breakBlock = true;

            for (BlockBreakerFeature feature : this.features) {
                if (!feature.allowBreak(context)) {
                    breakBlock = false;
                }
            }

            boolean empty = mainHandStack.isEmpty();

            if (breakBlock) {
                this.breakBlock(mutable, currentState, world, player, mainHandStack, !internal.contains(blockPose), canHarvest);
                this.exhaustionConsumer.consume(player);
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
        CollectedResult result = this.collector.finish();
        List<ItemStack> droppedStacks = this.dropper.dropStack(context.getWorld(), context.getOriginPos(), result);

        this.forEachFeatures(b -> b.afterCycle(context, droppedStacks, result.getMap()));

        //this.makeUndoForPlayer(info, linkedOpenHashMap, droppedStacks);
        if (experience <= 0) {
            return false;
        }
        ExperienceOrbEntity experienceOrbEntity = new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, experience);
        (serverWorld).spawnEntityAndPassengers(experienceOrbEntity);
        return false;
    }

    private void forEachFeatures(Consumer<BlockBreakerFeature> consumer) {
        if (features.isEmpty()) return;
        this.features.forEach(consumer);
    }

    private void breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest) {

        try {
            //world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
            blockBreaking.set(true);
        /*int flag = update ? Block.NOTIFY_ALL : Block.NOTIFY_LISTENERS;
        state.getBlock().onBreak(world, pos, state, player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), flag);
        if (!player.isCreative()) {
            mainHandStack.postMine(world, state, pos, player);
        }*/
            this.blockBreaker.breakBlock(pos, state, world, player, mainHandStack, update, canHarvest);
        } finally {
            blockBreaking.set(false);
        }
    }
}
