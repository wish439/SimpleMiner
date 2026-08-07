package com.wishtoday.ts.simpleminer.undo.network.payloads;

import com.wishtoday.ts.simpleminer.undo.UndoData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UndoDataSyncS2CPayload(int syncId, UndoData undoData) implements CustomPayload {
    public static final Id<UndoDataSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "undo_data_sync_payload"));

    public static final PacketCodec<RegistryByteBuf, UndoDataSyncS2CPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, UndoDataSyncS2CPayload::syncId, UndoData.PACKET_CODEC, UndoDataSyncS2CPayload::undoData, UndoDataSyncS2CPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
