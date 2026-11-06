package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollectorResult;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;

public interface ItemStackCollector {
    void clear();
    void collectItemStack(ItemStack stack);
    ItemCollectorResult toResult();
    Object2IntOpenHashMap<ItemStackKey> getMap();
}
