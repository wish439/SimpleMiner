package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectedResult;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ExperienceCollector;
import com.wishtoday.ts.simpleminer.mixin.Accessor.ExperienceDroppingBlockAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.World;

import java.util.List;

@Name("PUREAPI")
@Service
public class PureAPIItemCollector implements DroppedCollector {

    private final ItemStackCollector stackCollector;

    private final ExperienceCollector experienceCollector;

    @CreateConstruction
    public PureAPIItemCollector(ExperienceCollector experienceCollector) {
        this.experienceCollector = experienceCollector;
        this.stackCollector = new ItemStackCollector();
    }

    @Override
    public void start() {
        this.stackCollector.clear();
        this.experienceCollector.initialize();
    }

    @Override
    public boolean shouldCollect(CollectContext context) {
        return context.getPos() != null
                && context.getPlayer() != null
                && context.getWorld() != null
                && context.getHandStack() != null
                && context.getEntity() == null;
    }

    @Override
    public void collectItem(CollectContext context) {
        this.collectItemStack(context);
    }

    @Override
    public CollectedResult finish() {
        return new CollectedResult(new Object2IntOpenHashMap<>(this.stackCollector.getMap()), this.experienceCollector.getExperience());
    }

    @Override
    public boolean shouldApplyMixin(CollectContext context, String mixinName) {
        return context.getPos() != null
                && context.getPlayer() == null
                && context.getWorld() != null
                && context.getHandStack() == null
                && context.getEntity() == null
                && BlockBreaker.getBlockBreaking()
                && mixinName.startsWith("PUREAPI");
    }

    @Override
    public void collectExperience(CollectContext context) {
        BlockState currentState = context.getBlockState();
        World world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;
        ItemStack mainHandStack = context.getHandStack();
        if (currentState == null) return;
        if (mainHandStack == null) return;
        Block block = currentState.getBlock();
        int base;
        if (block instanceof ExperienceDroppingBlock e) {
            ExperienceDroppingBlockAccessor accessor = (ExperienceDroppingBlockAccessor) e;
            IntProvider experienceDropped = accessor.getExperienceDropped();
            base = experienceDropped.get(serverWorld.getRandom());
        } else {
            base = 0;
        }
        int experience = EnchantmentHelper.getBlockExperience(serverWorld, mainHandStack, base);
        this.experienceCollector.consumeExperience(experience);
    }

    public void collectItemStack(CollectContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getPos();
        boolean b = context.isCanHarvest();
        BlockState state = context.getBlockState();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        this.collectItemStack(world, player, state, blockEntity, context.getHandStack(), pos, this.stackCollector.getMap(), b);
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
}
