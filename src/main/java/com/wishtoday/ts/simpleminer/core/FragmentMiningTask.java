package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.ts.simpleminer.GlobalCondition;
import com.wishtoday.ts.simpleminer.core.blockBreaker.*;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ExternalStorageAdvocatedCollectorRouter;
import com.wishtoday.ts.simpleminer.core.blockBreaker.singleBlockBreaker.SingleBlockBreakerRouter;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.schedule.Task;
import com.wishtoday.ts.simpleminer.schedule.TickScheduler;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//TODO: Let it have full functionality.
public class FragmentMiningTask implements Task {
    private final ExternalStorageAdvocatedCollectorRouter collector;
    private final ItemDropper dropper;
    private final SingleBlockBreakerRouter blockBreaker;
    private final ExhaustionConsumer exhaustionConsumer;
    private final PlayerEntity player;
    private final World world;
    private final BlockPos initialPos;
    private final LongList sortedBlockPoses;
    private final int fragmentSize;
    private final int currentIndex;
    private final TickScheduler scheduler;

    public FragmentMiningTask(ExternalStorageAdvocatedCollectorRouter collector
            , ItemDropper dropper
            , SingleBlockBreakerRouter blockBreaker
            , ExhaustionConsumer exhaustionConsumer
            , PlayerEntity player
            , World world
            , BlockPos initialPos
            , LongList sortedBlockPoses
            , int fragmentSize
            , int currentIndex
            , TickScheduler scheduler
    ) {
        this.collector = collector;
        this.dropper = dropper;
        this.blockBreaker = blockBreaker;
        this.exhaustionConsumer = exhaustionConsumer;
        this.player = player;
        this.world = world;
        this.initialPos = initialPos;
        this.sortedBlockPoses = sortedBlockPoses;
        this.fragmentSize = fragmentSize;
        this.currentIndex = currentIndex;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        if (this.currentIndex > this.sortedBlockPoses.size()) {
            return;
        }
        if (GlobalCondition.isBlockBreaking()) {
            return;
        }
        LongList longList = sortedBlockPoses.subList(currentIndex, Math.min(currentIndex + fragmentSize, sortedBlockPoses.size() - 1));

        ItemStack mainHandStack = player.getMainHandStack();
        SimpleItemStackCollector itemStackCollector = new SimpleItemStackCollector();
        ExperienceCollector experienceCollector = new ExperienceCollector();

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        CollectContext collectContext = new CollectContext(world, player, null, mainHandStack, null, null, false);
        ServerWorld serverWorld = (ServerWorld) world;
        MAINCYCLE:
        for (long blockPose : longList) {
            mutable.set(blockPose);
            BlockState currentState = ((WorldExtension) world).simpleMiner$getBlockState(blockPose);
            collectContext.setBlockState(currentState);


            collectContext.setPos(mutable);
            boolean canHarvest = player.canHarvest(currentState);
            collectContext.setCanHarvest(canHarvest);

            if (this.collector.shouldCollect(collectContext)) {
                this.collector.collectItem(collectContext, itemStackCollector);
                this.collector.collectExperience(collectContext, experienceCollector);
            }


            boolean empty = mainHandStack.isEmpty();

            boolean b = this.breakBlock(mutable, currentState, world, player, mainHandStack, true, canHarvest);
            if (canHarvest && b) {
                this.exhaustionConsumer.consume(player);
            }


            if (!empty && player.getMainHandStack().isEmpty()) {
                break;
            }
        }
        this.dropper.dropStack(world, initialPos, itemStackCollector.getMap());


        if (experienceCollector.getExperience() > 0) {
            ExperienceOrbEntity experienceOrbEntity = new ExperienceOrbEntity(world, initialPos.getX() + 0.5D, initialPos.getY() + 0.5D, initialPos.getZ() + 0.5D, experienceCollector.getExperience());
            (serverWorld).spawnEntityAndPassengers(experienceOrbEntity);
            return;
        }

        scheduler.scheduleNextTick(new FragmentMiningTask(this.collector, dropper, blockBreaker, exhaustionConsumer, player, world, initialPos, sortedBlockPoses, this.fragmentSize, this.currentIndex + this.fragmentSize, scheduler));
    }

    private boolean breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update, boolean canHarvest) {
        try {
            GlobalCondition.setBlockBreaking(true);
            return this.blockBreaker.breakBlock(pos, state, world, player, mainHandStack, update, canHarvest);
        } finally {
            GlobalCondition.setBlockBreaking(false);
        }
    }
}
