package com.wishtoday.ts.simpleminer.undo.network.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class UndoListSyncRequestC2SPayload implements CustomPayload {
    public static final Id<UndoListSyncRequestC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "undo_list_sync_request_c2s"));
    public static final PacketCodec<PacketByteBuf, UndoListSyncRequestC2SPayload> CODEC = PacketCodec.of((value, buf) -> {}, buf -> new UndoListSyncRequestC2SPayload());
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
