package com.wishtoday.ts.simpleminer.undo;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record UndoDisplayInfo(String text, long time) {
    public static final PacketCodec<PacketByteBuf, UndoDisplayInfo> PACKET_CODEC = PacketCodec.of(UndoDisplayInfo::write, UndoDisplayInfo::read);
    private void write(PacketByteBuf buf) {
        buf.writeString(text);
        buf.writeLong(time);
    }
    private static UndoDisplayInfo read(PacketByteBuf buf) {
        String text = buf.readString();
        long time = buf.readLong();
        return new UndoDisplayInfo(text, time);
    }
}
