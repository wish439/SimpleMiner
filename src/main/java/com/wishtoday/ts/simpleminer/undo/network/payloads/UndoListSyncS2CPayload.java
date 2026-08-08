package com.wishtoday.ts.simpleminer.undo.network.payloads;

import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record UndoListSyncS2CPayload(List<UndoDisplayInfo> displayInfos) implements CustomPayload {
    public static final Id<UndoListSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "undo_list_sync_s2c_payload"));
    private static final PacketCodec<RegistryByteBuf, List<UndoDisplayInfo>> LIST_PACKET_CODEC = PacketCodecs.collection(i -> new ArrayList<>(), UndoDisplayInfo.PACKET_CODEC);
    public static final PacketCodec<RegistryByteBuf, UndoListSyncS2CPayload> CODEC = PacketCodec.tuple(LIST_PACKET_CODEC, UndoListSyncS2CPayload::displayInfos, UndoListSyncS2CPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
