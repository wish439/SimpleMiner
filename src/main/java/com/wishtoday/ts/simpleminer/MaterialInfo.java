package com.wishtoday.ts.simpleminer;

import lombok.Data;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

@Data
public class MaterialInfo {
    private final ItemStack itemStack;
    private final int maxCount;
    private int currentCount;

    public MaterialInfo(Item itemStack, int maxCount, int currentCount) {
        this.itemStack = new ItemStack(itemStack);
        this.maxCount = maxCount;
        this.currentCount = currentCount;
    }

    public MaterialInfo(ItemStack itemStack, int maxCount, int currentCount) {
        this.itemStack = itemStack.copyWithCount(1);
        this.maxCount = maxCount;
        this.currentCount = currentCount;
    }

    public void addCurrentCount(int currentCount) {
        this.currentCount += currentCount;
    }

    public static final PacketCodec<RegistryByteBuf, MaterialInfo> PACKET_CODEC = PacketCodec.of(MaterialInfo::encode, MaterialInfo::decode);

    private void encode(RegistryByteBuf buf) {
        ItemStack.PACKET_CODEC.encode(buf, itemStack);
        buf.writeInt(maxCount);
        buf.writeInt(currentCount);
    }

    private static MaterialInfo decode(RegistryByteBuf buf) {
        ItemStack stack = ItemStack.PACKET_CODEC.decode(buf);
        int maxCount = buf.readInt();
        int currentCount = buf.readInt();
        return new MaterialInfo(stack, maxCount, currentCount);
    }

    public boolean isFinished() {
        return currentCount >= maxCount;
    }
}
