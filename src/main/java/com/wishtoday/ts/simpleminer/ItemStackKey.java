package com.wishtoday.ts.simpleminer;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

public class ItemStackKey {
    private final ItemStack itemStack;
    private final int hash;

    public ItemStackKey(ItemStack itemStack) {
        ItemStack stack = itemStack.copyWithCount(1);
        this.itemStack = stack;
        this.hash = ItemStack.hashCode(stack);
    }
    public static final PacketCodec<RegistryByteBuf, ItemStackKey> PACKET_CODEC = PacketCodec.of((value, buf) -> ItemStack.PACKET_CODEC.encode(buf,value.itemStack), buf -> new ItemStackKey(ItemStack.PACKET_CODEC.decode(buf)));

    public ItemStack itemStack() {
        return this.itemStack;
    }

    public int hash() {
        return this.hash;
    }
    
    @Override
    public @NotNull String toString() {
        return itemStack.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStackKey key)) return false;
        return ItemStack.areItemsAndComponentsEqual(this.itemStack, key.itemStack);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
