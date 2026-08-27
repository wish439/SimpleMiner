package com.wishtoday.ts.simpleminer;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

public record ItemStackKey(ItemStack itemStack, int hash) {

    public ItemStackKey(ItemStack itemStack) {
        this(itemStack.copyWithCount(1), ItemStack.hashCode(itemStack));
    }
    public static final PacketCodec<RegistryByteBuf, ItemStackKey> PACKET_CODEC = PacketCodec.of((value, buf) -> ItemStack.PACKET_CODEC.encode(buf,value.itemStack), buf -> new ItemStackKey(ItemStack.PACKET_CODEC.decode(buf)));

    @Override
    public @NotNull String toString() {
        return itemStack.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStackKey(ItemStack stack, int hash1))) return false;
        return ItemStack.areItemsAndComponentsEqual(this.itemStack, stack);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
