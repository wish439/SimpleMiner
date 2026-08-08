package com.wishtoday.ts.simpleminer.undo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UndoDisplayInfo {
    private final String text;
    private final long time;
    private final UUID uuid;
    private final List<ItemStack> stacks;
    private final boolean hasRemainMaterials;
    public static final PacketCodec<RegistryByteBuf, UndoDisplayInfo> PACKET_CODEC = PacketCodec.of(UndoDisplayInfo::write, UndoDisplayInfo::read);

    private void write(RegistryByteBuf buf) {
        buf.writeString(text);
        buf.writeLong(time);
        buf.writeUuid(uuid);
        ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, stacks);
        buf.writeBoolean(hasRemainMaterials);
    }

    private static UndoDisplayInfo read(RegistryByteBuf buf) {
        String text = buf.readString();
        long time = buf.readLong();
        UUID read = buf.readUuid();
        return new UndoDisplayInfo(text, time, read, ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf), buf.readBoolean());
    }
}
