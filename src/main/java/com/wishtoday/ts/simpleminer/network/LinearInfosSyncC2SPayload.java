package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LinearInfosSyncC2SPayload(LinearShapeInfos infos) implements CustomPayload {
    public static final Id<LinearInfosSyncC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "linear_infos_sync_c2s_payload"));
    public static final PacketCodec<PacketByteBuf, LinearInfosSyncC2SPayload> CODEC = PacketCodec.tuple(LinearShapeInfos.PACKET_CODEC, LinearInfosSyncC2SPayload::infos, LinearInfosSyncC2SPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
