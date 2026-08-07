package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

@Name("PUREAPI")
@Service
public class BlockDroppedDropper implements ItemCollector{

    private final Object2IntOpenHashMap<ItemStackKey> map;

    @CreateConstruction
    public BlockDroppedDropper() {
        this.map = new Object2IntOpenHashMap<>();
    }

    @Override
    public void start() {
        this.map.clear();
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
        return new CollectedResult(new Object2IntOpenHashMap<>(this.map));
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

    public void collectItemStack(CollectContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();
        boolean b = player.canHarvest(world.getBlockState(pos));
        this.collectItemStack(world, player, context.getHandStack(), pos, this.map, b);
    }

    private void collectItemStack(World world, PlayerEntity player, ItemStack stack, BlockPos pos, Object2IntOpenHashMap<ItemStackKey> map, boolean toolFit) {
        if (toolFit) {
            this.insertItemStack(world, player, pos, stack, map);
        }
        this.insertContainerItemStack(world, pos, player, map);
    }

    private void insertItemStack(World world, PlayerEntity player, BlockPos blockPose, ItemStack mainHandStack, Object2IntOpenHashMap<ItemStackKey> map) {
        List<ItemStack> droppedStacks = Block.getDroppedStacks(world.getBlockState(blockPose), (ServerWorld) world, blockPose, world.getBlockEntity(blockPose), player, mainHandStack);
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
}
