package com.wishtoday.ts.simpleminer.utils;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackUtils {
    public static List<ItemStack> spiltDroppedStacks(Object2IntOpenHashMap<ItemStackKey> map) {
        List<ItemStack> result = new ArrayList<>();
        for (Object2IntMap.Entry<ItemStackKey> itemStackEntry : map.object2IntEntrySet()) {
            int count = itemStackEntry.getIntValue();
            ItemStackKey item = itemStackEntry.getKey();
            List<ItemStack> stack = splitItemStack(item, count);
            result.addAll(stack);
        }
        return result;
    }

    private static List<ItemStack> splitItemStack(ItemStackKey item, int count) {
        List<ItemStack> stacks = new ArrayList<>();
        int max = item.itemStack().getMaxCount();
        long l = MathUtils.fastDivide(count, max);
        int l1 = (int) (l >> 32);
        int l2 = (int) (l & 0xFFFFFFFFL);
        for (int i = 0; i < l1; i++) {
            stacks.add(item.itemStack().copyWithCount(max));
        }
        if (l2 != 0) stacks.add(item.itemStack().copyWithCount(l2));
        return stacks;
    }
}
