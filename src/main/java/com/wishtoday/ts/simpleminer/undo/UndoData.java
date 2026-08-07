package com.wishtoday.ts.simpleminer.undo;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.HashMap;
import java.util.Map;

public record UndoData(Map<ItemStackKey, MaterialInfo> map, int completedCount, int index) {
    private static final PacketCodec<RegistryByteBuf, Map<ItemStackKey, MaterialInfo>> MAP_PACKET_CODEC = PacketCodecs.map(HashMap::new, ItemStackKey.PACKET_CODEC, MaterialInfo.PACKET_CODEC);
    public static final PacketCodec<RegistryByteBuf, UndoData> PACKET_CODEC = PacketCodec.of(UndoData::write, UndoData::read);

    private void write(RegistryByteBuf buf) {
        MAP_PACKET_CODEC.encode(buf, this.map);
        buf.writeVarInt(this.completedCount);
        buf.writeVarInt(this.index);
    }

    private static UndoData read(RegistryByteBuf buf) {
        Map<ItemStackKey, MaterialInfo> map = MAP_PACKET_CODEC.decode(buf);
        int completedCount = buf.readVarInt();
        int index = buf.readVarInt();
        return new UndoData(map, completedCount, index);
    }
}
