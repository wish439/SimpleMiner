package com.wishtoday.ts.simpleminer.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record KeywordPressedPayload(boolean press, int shapeIndex) implements CustomPayload {
    public static final Id<KeywordPressedPayload> ID = new Id<>(Identifier.of("simpleminer","keyword_pressed"));
    public static final PacketCodec<PacketByteBuf, KeywordPressedPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBoolean(value.press);
        buf.writeInt(value.shapeIndex);
    }, buf -> new KeywordPressedPayload(buf.readBoolean(), buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
