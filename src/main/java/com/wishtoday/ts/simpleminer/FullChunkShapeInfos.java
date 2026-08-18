package com.wishtoday.ts.simpleminer;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.PacketCodec;

@Data
public class FullChunkShapeInfos {
    public static final FullChunkShapeInfos DEFAULT = new FullChunkShapeInfos(0, 0);
    private int radiusX;
    private int radiusZ;

    public static final PacketCodec<ByteBuf, FullChunkShapeInfos> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.radiusX);
        buf.writeInt(value.radiusZ);
    }, buf -> new FullChunkShapeInfos(buf.readInt(), buf.readInt()));

    public FullChunkShapeInfos(int radiusX, int radiusZ) {
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
    }

    public FullChunkShapeInfos copy() {
        return new FullChunkShapeInfos(radiusX, radiusZ);
    }
}
