package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollectorResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.minecraft.item.ItemStack;

public class SimpleItemStackCollector implements ItemStackCollector {
    @Getter
    private final Object2IntOpenHashMap<ItemStackKey> map;

    public SimpleItemStackCollector() {
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

    public ItemCollectorResult toResult() {
        return new ItemCollectorResult(new Object2IntOpenHashMap<>(this.map));
    }

    @Override
    public void append(ItemStackCollector collector) {
        for (Object2IntMap.Entry<ItemStackKey> entry : collector.getMap().object2IntEntrySet()) {
            this.map.addTo(entry.getKey(), entry.getIntValue());
        }
    }
}
