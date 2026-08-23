package com.wishtoday.ts.simpleminer.undo.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record DeleteUndoC2SPayload(UUID undoUuid) implements CustomPayload {
    public static final Id<DeleteUndoC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "delete_undo_c2s_payload"));
    public static final PacketCodec<PacketByteBuf, DeleteUndoC2SPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeUuid(payload.undoUuid()),
            buf -> new DeleteUndoC2SPayload(buf.readUuid())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
