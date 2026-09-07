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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
//Emmm, because the weather here is pretty hot, the code might not be that easy to understand/look nice,
//and there might be some small bugs.
//In short, based on my tests, his speed might be about 30%-50% faster than the PureAPI mode.
@Service
@Name("EXPERIMENTAL")
public class ExperimentalPureAPIItemCollector implements ItemCollector {
    private final ItemStackCollector stackCollector;
    private final Map<BlockState, SampleEntry> samples;
    private static final int SAMPLE_COUNT = 45;

    @CreateConstruction
    public ExperimentalPureAPIItemCollector() {
        this.stackCollector = new ItemStackCollector();
        this.samples = new HashMap<>();
    }

    @Override
    public void start() {
        this.stackCollector.clear();
        this.samples.clear();
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
        Set<Map.Entry<BlockState, SampleEntry>> set = this.samples.entrySet();
        for (Map.Entry<BlockState, SampleEntry> entry : set) {
            SampleEntry value = entry.getValue();
            if (!value.successful) continue;
            Set<Map.Entry<ItemStackKey, SampleEntry.Counter>> entries = value.counterMap.entrySet();
            int matchCount = value.matchCount;
            for (Map.Entry<ItemStackKey, SampleEntry.Counter> counterEntry : entries) {
                map.put(counterEntry.getKey(), counterEntry.getValue().firstCount * matchCount);
            }
        }
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
        BlockState blockState = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        boolean b = player.canHarvest(blockState);
        SampleEntry sampleEntry = this.samples.computeIfAbsent(blockState, q -> new SampleEntry());
        if (sampleEntry.successful) {
            if (!b) return;
            sampleEntry.addMatchCount(1);
            return;
        }
        if (sampleEntry.matchCount >= SAMPLE_COUNT && sampleEntry.isStable) {
            sampleEntry.successful = true;
            if (!b) return;
            sampleEntry.addMatchCount(1);
            return;
        }
        Object2IntOpenHashMap<ItemStackKey> map = new Object2IntOpenHashMap<>();
        this.collectItemStack(world, blockState, player, context.getHandStack(), pos, map, b);
        sampleEntry.match(map);
        sampleEntry.addMatchCount(1);
        Object2IntMap.FastEntrySet<ItemStackKey> entries = map.object2IntEntrySet();
        Object2IntOpenHashMap<ItemStackKey> collectorMap = this.stackCollector.getMap();
        for (Object2IntMap.Entry<ItemStackKey> entry : entries) {
            collectorMap.addTo(entry.getKey(), entry.getIntValue());
        }
    }

    private void collectItemStack(World world, BlockState state, PlayerEntity player, ItemStack stack, BlockPos pos, Object2IntOpenHashMap<ItemStackKey> map, boolean toolFit) {
        if (toolFit) {
            this.insertItemStack(world, state, player, pos, stack, map);
        }
        this.insertContainerItemStack(world, pos, player, map);
    }

    private void insertItemStack(World world, BlockState state, PlayerEntity player, BlockPos blockPose, ItemStack mainHandStack, Object2IntOpenHashMap<ItemStackKey> map) {
        List<ItemStack> droppedStacks = Block.getDroppedStacks(state, (ServerWorld) world, blockPose, world.getBlockEntity(blockPose), player, mainHandStack);
        for (ItemStack stack : droppedStacks) {
            int count = stack.getCount();
            ItemStackKey key = new ItemStackKey(stack);
            //System.out.println("AAABBBCCC:::" + key);
            map.addTo(key, count);
        }
    }

    private void insertContainerItemStack(World world, BlockPos pos, PlayerEntity player, Object2IntOpenHashMap<ItemStackKey> map) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
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
