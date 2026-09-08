package com.wishtoday.ts.simpleminer.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record RenderTextSyncS2CPayload(List<Text> texts) implements CustomPayload {
    public static final Id<RenderTextSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "render_text_sync_s2c_payload"));
    public static final PacketCodec<RegistryByteBuf, List<Text>> TEXT_LIST_CODEC = PacketCodecs.collection(ArrayList::new, TextCodecs.PACKET_CODEC);
    public static final PacketCodec<RegistryByteBuf, RenderTextSyncS2CPayload> CODEC = PacketCodec.of((value, buf) -> {
        TEXT_LIST_CODEC.encode(buf, value.texts);
    }, buf -> new RenderTextSyncS2CPayload(TEXT_LIST_CODEC.decode(buf)));
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
