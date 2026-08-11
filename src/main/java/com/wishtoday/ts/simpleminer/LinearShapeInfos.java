package com.wishtoday.ts.simpleminer;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

@Data
public class LinearShapeInfos {
    public static final LinearShapeInfos DEFAULT = new LinearShapeInfos(1, 1);

    private int width;
    private int height;

    public static final PacketCodec<ByteBuf, LinearShapeInfos> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, LinearShapeInfos::getHeight, PacketCodecs.VAR_INT, LinearShapeInfos::getWidth, LinearShapeInfos::new);

    public LinearShapeInfos(int height, int width) {
        this.width = width;
        this.height = height;
    }

    public LinearShapeInfos copy() {
        return new LinearShapeInfos(height, width);
    }
}
