package com.wishtoday.ts.simpleminer.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public record OtherTextSyncS2CPayload(Int2ObjectOpenHashMap<List<Text>> map) implements CustomPayload {
    public static final Id<OtherTextSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "other_text_sync_s2c_payload"));
    private static final PacketCodec<RegistryByteBuf, Int2ObjectOpenHashMap<List<Text>>> MAP_PACKET_CODEC = PacketCodecs.map(Int2ObjectOpenHashMap::new, PacketCodecs.INTEGER, RenderTextSyncS2CPayload.TEXT_LIST_CODEC);
    public static final PacketCodec<RegistryByteBuf, OtherTextSyncS2CPayload> CODEC = PacketCodec.tuple(MAP_PACKET_CODEC,OtherTextSyncS2CPayload::map, OtherTextSyncS2CPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
