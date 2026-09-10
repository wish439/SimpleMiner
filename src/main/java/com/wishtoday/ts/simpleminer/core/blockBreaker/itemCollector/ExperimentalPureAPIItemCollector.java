package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectedResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

@Service
@Name("EXPERIMENTAL")
public class ExperimentalPureAPIItemCollector implements ItemCollector {
    private final ItemStackCollector stackCollector;
    private final ProbabilityItemTester tester;
    //private final Map<BlockState, SampleEntry> samples;

    @CreateConstruction
    public ExperimentalPureAPIItemCollector(ProbabilityItemTester tester) {
        this.tester = tester;
        this.stackCollector = new ItemStackCollector();
    }

    @Override
    public void start() {
        this.stackCollector.clear();
        this.tester.initialize();
    }

    @Override
    public boolean shouldCollectItem(CollectContext context) {
        return context.getPos() != null
                && context.getPlayer() != null
                && context.getWorld() != null
                && context.getHandStack() != null
                && context.getItemEntity() == null;
    }

    @Override
    public void collectItem(CollectContext context) {
        this.collectItemStack(context);
    }

    @Override
    public CollectedResult finish() {
        Object2IntOpenHashMap<ItemStackKey> map = new Object2IntOpenHashMap<>(this.stackCollector.getMap());
        Object2IntOpenHashMap<ItemStackKey> temp = this.tester.end();
        map.putAll(temp);
        return new CollectedResult(map);
    }

    @Override
    public boolean shouldApplyMixin(CollectContext context, String mixinName) {
        return context.getPos() != null
                && context.getPlayer() == null
                && context.getWorld() != null
                && context.getHandStack() == null
                && context.getItemEntity() == null
                && mixinName.startsWith("PUREAPI");
    }

    private void collectItemStack(CollectContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState blockState = context.getBlockState();
        PlayerEntity player = context.getPlayer();
        boolean b = context.isCanHarvest();
        this.tester.incrementBlockCount(blockState, b);
        if (!this.tester.shouldContinueGetDropped(blockState)) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        Object2IntOpenHashMap<ItemStackKey> map = new Object2IntOpenHashMap<>();
        this.collectItemStack(world, player, blockState, blockEntity, context.getHandStack(), pos, map, b);
        this.tester.matchOneBlock(blockState, map);
        Object2IntMap.FastEntrySet<ItemStackKey> entries = map.object2IntEntrySet();
        Object2IntOpenHashMap<ItemStackKey> collectorMap = this.stackCollector.getMap();
        for (Object2IntMap.Entry<ItemStackKey> entry : entries) {
            collectorMap.addTo(entry.getKey(), entry.getIntValue());
        }
    }

    private void collectItemStack(World world, PlayerEntity player, BlockState currentBlockState, BlockEntity entity, ItemStack stack, BlockPos pos, Object2IntOpenHashMap<ItemStackKey> map, boolean toolFit) {
        if (toolFit) {
            this.insertItemStack(world, player, currentBlockState, entity, pos, stack, map);
        }
        this.insertContainerItemStack(world, pos, entity, player, map);
    }

    private void insertItemStack(World world, PlayerEntity player, BlockState currentBlockState, BlockEntity blockEntity, BlockPos blockPose, ItemStack mainHandStack, Object2IntOpenHashMap<ItemStackKey> map) {
        List<ItemStack> droppedStacks = Block.getDroppedStacks(currentBlockState, (ServerWorld) world, blockPose, blockEntity, player, mainHandStack);
        for (ItemStack stack : droppedStacks) {
            int count = stack.getCount();
            ItemStackKey key = new ItemStackKey(stack);
            //System.out.println("AAABBBCCC:::" + key);
            if (map.containsKey(key)) {
                map.addTo(key, count);
            } else {
                map.put(key, count);
            }
        }
    }

    private void insertContainerItemStack(World world, BlockPos pos, BlockEntity blockEntity, PlayerEntity player, Object2IntOpenHashMap<ItemStackKey> map) {
        if (blockEntity == null) return;
        if (!(blockEntity instanceof Inventory inventory)) return;
        if (inventory.isEmpty()) return;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            ItemStackKey itemStack = new ItemStackKey(stack);
            if (map.containsKey(itemStack)) {
                map.addTo(itemStack, stack.getCount());
            } else {
                map.put(itemStack, stack.getCount());
            }
        }
    }


    private static class SampleEntry {
        private int matchCount;
        private final Map<ItemStackKey, Counter> counterMap;
        private boolean isStable;
        private boolean successful;
        private int firstItemCount;

        private SampleEntry() {
            this.matchCount = 0;
            this.counterMap = new HashMap<>();
            this.successful = false;
            this.isStable = true;
            this.firstItemCount = -1;
        }

        public boolean isStable(int maxSampleCount) {
            if (!isStable) return false;
            Collection<Counter> values = counterMap.values();
            for (Counter value : values) {
                if (value.admitted < maxSampleCount) return false;
            }
            return true;
        }

        public void addMatchCount(int count) {
            this.matchCount += count;
        }

        public void match(Object2IntOpenHashMap<ItemStackKey> map) {
            if (!isStable) {
                return;
            }
            Object2IntMap.FastEntrySet<ItemStackKey> entries = map.object2IntEntrySet();
            if (this.firstItemCount == -1) this.firstItemCount = map.size();
            if (map.size() != this.firstItemCount) {
                this.isStable = false;
                return;
            }
            for (Object2IntMap.Entry<ItemStackKey> entry : entries) {
                ItemStackKey key = entry.getKey();
                int intValue = entry.getIntValue();
                boolean b = this.counterMap.computeIfAbsent(key, k -> new Counter(intValue))
                        .tryAdmit(intValue);
                if (!b) {
                    this.isStable = false;
                }
            }
        }

        @Getter
        private static class Counter {
            private final int firstCount;
            private int admitted;
            public Counter(int firstCount) {
                this.firstCount = firstCount;
                this.admitted = 1;
            }

            public boolean tryAdmit(int current) {
                if (this.firstCount == current) {
                    this.admitted++;
                    return true;
                }
                return false;
            }
        }
    }
}
