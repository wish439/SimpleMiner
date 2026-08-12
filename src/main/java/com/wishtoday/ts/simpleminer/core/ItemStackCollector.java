package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectedResult;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.minecraft.item.ItemStack;

public class ItemStackCollector {
    @Getter
    private final Object2IntOpenHashMap<ItemStackKey> map;

    public ItemStackCollector() {
        map = new Object2IntOpenHashMap<>();
    }

    public void clear() {
        this.map.clear();
    }

    public void collectItemStack(ItemStack stack) {
        int count = stack.getCount();
        ItemStackKey key = new ItemStackKey(stack);
        if (map.containsKey(key)) {
            map.addTo(key, count);
        } else {
            map.put(key, count);
        }
    }

    public CollectedResult toResult() {
        return new CollectedResult(new Object2IntOpenHashMap<>(this.map));
    }
}
