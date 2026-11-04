package com.wishtoday.ts.simpleminer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;

public class ItemStack2ObjectHashMap<V> extends Object2ObjectOpenCustomHashMap<ItemStack, V> {
    public ItemStack2ObjectHashMap(int expected, float f) {
        super(expected, f, STRATEGY);
    }

    private static final Strategy<? super ItemStack> STRATEGY = new Strategy<>() {
        @Override
        public int hashCode(ItemStack o) {
            return ItemStack.hashCode(o);
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            return ItemStack.areItemsAndComponentsEqual(a, b);
        }
    };

    @Override
    public V put(ItemStack stack, V v) {
        if (stack == null) {
            return super.put(null, v);
        }
        return super.put(stack.copyWithCount(1), v);
    }
}
