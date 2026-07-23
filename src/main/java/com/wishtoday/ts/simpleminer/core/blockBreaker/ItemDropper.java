package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.utils.ItemStackUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemDropper {
    public List<ItemStack> dropStack(BlockBreakContext context, Object2IntOpenHashMap<ItemStackKey> map) {
        World world = context.world();
        BlockPos pos = context.pos();
        List<ItemStack> droppedStacks = this.mergeAndSpiltDroppedStacks(map);
        for (ItemStack itemStack : droppedStacks) {
            Block.dropStack(world, pos, itemStack);
        }
        return droppedStacks;
    }

    private List<ItemStack> mergeAndSpiltDroppedStacks(Object2IntOpenHashMap<ItemStackKey> map) {
        return ItemStackUtils.spiltDroppedStacks(map);
    }
}
