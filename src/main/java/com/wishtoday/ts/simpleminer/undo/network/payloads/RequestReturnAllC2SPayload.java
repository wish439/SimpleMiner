package com.wishtoday.ts.simpleminer.undo.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestReturnAllC2SPayload(int syncId) implements CustomPayload {
    public static final Id<RequestReturnAllC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "request_return_all_c2s_payload"));
    public static final PacketCodec<PacketByteBuf, RequestReturnAllC2SPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, RequestReturnAllC2SPayload::syncId, RequestReturnAllC2SPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
