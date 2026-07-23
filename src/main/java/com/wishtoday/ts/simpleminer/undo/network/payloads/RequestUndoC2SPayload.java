package com.wishtoday.ts.simpleminer.undo.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestUndoC2SPayload(int syncId) implements CustomPayload {
    public static final CustomPayload.Id<RequestUndoC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "request_undo_c2s_payload"));
    public static final PacketCodec<PacketByteBuf, RequestUndoC2SPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, RequestUndoC2SPayload::syncId, RequestUndoC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
