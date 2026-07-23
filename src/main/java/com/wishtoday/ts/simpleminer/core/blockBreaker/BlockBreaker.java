package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.core.ShapeAnalyzer;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import com.wishtoday.ts.simpleminer.utils.MathUtils;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class BlockBreaker {
    private final PressManager pressManager;
    private final ShapeRefresher shapeRefresher;
    private final ShapeAnalyzer shapeAnalyzer;
    private final List<BlockBreakerFeature> features;
    private final ItemCollectorRouter collector;
    private final ItemDropper dropper;
    private static final ThreadLocal<Boolean> blockBreaking = ThreadLocal.withInitial(() -> false);

    @CreateConstruction
    public BlockBreaker(PressManager pressManager
            , ShapeRefresher shapeRefresher
            , List<BlockBreakerFeature> features
            , ShapeAnalyzer shapeAnalyzer
            , ItemCollectorRouter collector
            , ItemDropper dropper
    ) {
        this.pressManager = pressManager;
        this.shapeRefresher = shapeRefresher;
        this.shapeAnalyzer = shapeAnalyzer;
        this.features = features;
        this.collector = collector;
        this.dropper = dropper;
    }

    public static boolean getBlockBreaking() {
        return blockBreaking.get();
    }

    public boolean breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        ItemStack mainHandStack = player.getMainHandStack();
        PlayerMinerInfo info = this.pressManager.getPressedPlayerMinerInfo(player);
        if (info == null) return true;
        this.shapeRefresher.refresh(info, pos);
        ShapeResult shapeResult = info.getBlockPoses();
        if (shapeResult == null) return true;
        LongArrayList sortedBlockPoses = shapeResult.getSortedBlockPoses();
        LongOpenHashSet internal = shapeAnalyzer.calcCompleteSurrounded(shapeResult.getBlockPoses());
        BlockBreakContext context = new BlockBreakContext(world, player, pos, state, blockEntity, info, shapeResult, mainHandStack);

        this.forEachFeatures(b -> b.beforeCycle(context));

        this.collector.start();
        int experience = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        //Long2ObjectLinkedOpenHashMap<BlockStorage> linkedOpenHashMap = new Long2ObjectLinkedOpenHashMap<>();
        CollectContext collectContext = new CollectContext(world, player, null, mainHandStack, null);
        MAINCYCLE:
        for (long blockPose : sortedBlockPoses) {

            mutable.set(blockPose);

            this.forEachFeatures(b -> b.beforeBlockBreak(context, blockPose, mutable));

            collectContext.setPos(mutable);
            if (this.collector.shouldCollectItem(collectContext)) {
                this.collector.collectItem(collectContext);
            }

            experience += EnchantmentHelper.getBlockExperience((ServerWorld) world, mainHandStack, 0);

            boolean breakBlock = true;

            for (BlockBreakerFeature feature : this.features) {
                if (!feature.allowBreak(context, blockPose, mutable)) {
                    breakBlock = false;
                }
            }

            boolean empty = mainHandStack.isEmpty();

            if (breakBlock)
                this.breakBlock(mutable, ((WorldExtension) world).simpleMiner$getBlockState(blockPose), world, player, mainHandStack, !internal.contains(blockPose));


            if (!empty && player.getMainHandStack().isEmpty()) {
                break;
            }

            for (BlockBreakerFeature feature : this.features) {
                if (!feature.afterBlockBreakAllowContinue(context, blockPose, mutable)) {
                    break MAINCYCLE;
                }
            }
        }
        CollectedResult result = this.collector.finish();
        List<ItemStack> droppedStacks = this.dropper.dropStack(context, result.getMap());

        this.forEachFeatures(b -> b.afterCycle(context, droppedStacks, result.getMap()));

        //this.makeUndoForPlayer(info, linkedOpenHashMap, droppedStacks);
        if (experience <= 0) {
            return false;
        }
        ExperienceOrbEntity experienceOrbEntity = new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, experience);
        ((ServerWorld) world).spawnEntityAndPassengers(experienceOrbEntity);
        return false;
    }

    private void forEachFeatures(Consumer<BlockBreakerFeature> consumer) {
        if (features.isEmpty()) return;
        this.features.forEach(consumer);
    }

    private void breakBlock(BlockPos pos, BlockState state, World world, PlayerEntity player, ItemStack mainHandStack, boolean update) {
        //world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0);
        blockBreaking.set(true);
        int flag = update ? Block.NOTIFY_ALL : Block.NOTIFY_LISTENERS;
        state.getBlock().onBreak(world, pos, state, player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), flag);
        if (!player.isCreative()) {
            mainHandStack.postMine(world, state, pos, player);
        }
        blockBreaking.set(false);
    }
}
