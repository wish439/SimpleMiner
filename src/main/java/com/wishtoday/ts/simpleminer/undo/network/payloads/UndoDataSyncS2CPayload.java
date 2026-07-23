package com.wishtoday.ts.simpleminer.undo.network.payloads;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record UndoDataSyncS2CPayload(int syncId, Map<ItemStackKey, MaterialInfo> map, int completedCount) implements CustomPayload {
    public static final Id<UndoDataSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "undo_data_sync_payload"));
    private static final PacketCodec<RegistryByteBuf, Map<ItemStackKey, MaterialInfo>> MAP_PACKET_CODEC = PacketCodecs.map(HashMap::new, ItemStackKey.PACKET_CODEC, MaterialInfo.PACKET_CODEC);
    public static final PacketCodec<RegistryByteBuf, UndoDataSyncS2CPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, UndoDataSyncS2CPayload::syncId, MAP_PACKET_CODEC, UndoDataSyncS2CPayload::map, PacketCodecs.INTEGER, UndoDataSyncS2CPayload::completedCount, UndoDataSyncS2CPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
