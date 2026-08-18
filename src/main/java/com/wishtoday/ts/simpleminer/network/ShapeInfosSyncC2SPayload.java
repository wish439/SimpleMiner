package com.wishtoday.ts.simpleminer.network;

import com.wishtoday.ts.simpleminer.FullChunkShapeInfos;
import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShapeInfosSyncC2SPayload(int shapeIndex, Object info) implements CustomPayload{
    public static final CustomPayload.Id<ShapeInfosSyncC2SPayload> ID = new CustomPayload.Id<>(Identifier.of("simpleminer", "shape_infos_sync_c2s_payload"));
    public static final PacketCodec<PacketByteBuf, ShapeInfosSyncC2SPayload> CODEC = PacketCodec.of(ShapeInfosSyncC2SPayload::write, ShapeInfosSyncC2SPayload::read);

    private static void write(ShapeInfosSyncC2SPayload infos, PacketByteBuf buf) {
        buf.writeVarInt(infos.shapeIndex);
        Object info = infos.info;
        if (info instanceof FullChunkShapeInfos fullChunkShapeInfos) {
            FullChunkShapeInfos.PACKET_CODEC.encode(buf, fullChunkShapeInfos);
            return;
        }
        if (info instanceof LinearShapeInfos linearShapeInfos) {
            LinearShapeInfos.PACKET_CODEC.encode(buf, linearShapeInfos);
            return;
        }
    }

    private static ShapeInfosSyncC2SPayload read(PacketByteBuf buf) {
        int shapeIndex = buf.readVarInt();
        Object info;
        switch (shapeIndex) {
            case 1 -> info = LinearShapeInfos.PACKET_CODEC.decode(buf);
            case 2 -> info = FullChunkShapeInfos.PACKET_CODEC.decode(buf);
            default -> info = null;
        }
        return new ShapeInfosSyncC2SPayload(shapeIndex, info);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
