package com.wishtoday.ts.simpleminer.undo.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record RequestOpenSingleUndoScreenHandlerC2SPayload(UUID uuid) implements CustomPayload {
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final Id<RequestOpenSingleUndoScreenHandlerC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "request_open_undo_screen_handler_c2s_payload"));
    public static final PacketCodec<PacketByteBuf, RequestOpenSingleUndoScreenHandlerC2SPayload> CODEC = PacketCodec.tuple(Uuids.PACKET_CODEC, RequestOpenSingleUndoScreenHandlerC2SPayload::uuid, RequestOpenSingleUndoScreenHandlerC2SPayload::new);
}
