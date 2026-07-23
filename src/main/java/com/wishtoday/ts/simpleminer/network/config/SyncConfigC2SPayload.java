package com.wishtoday.ts.simpleminer.network.config;

import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncConfigC2SPayload(ConfigType type, Object config) implements CustomPayload {
    public static final CustomPayload.Id<SyncConfigC2SPayload> ID = new Id<>(Identifier.of("simpleminer","sync_config_c2s"));
    public static final PacketCodec<PacketByteBuf, SyncConfigC2SPayload> CODEC = PacketCodec.of(SyncConfigC2SPayload::encode, SyncConfigC2SPayload::decode);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    private static void encode(SyncConfigC2SPayload value, PacketByteBuf buf) {
        buf.writeEnumConstant(value.type);
        if (value.type == ConfigType.SERVER) {
            ServerConfig.CODEC.encode(buf, (ServerConfig) value.config);
        }
        if (value.type == ConfigType.INDIVIDUAL) {
            IndividualConfig.CODEC.encode(buf, (IndividualConfig) value.config);
        }
        //buf.writeVarInt(value.syncId);
    }

    private static SyncConfigC2SPayload decode(PacketByteBuf buf) {
        ConfigType configType = buf.readEnumConstant(ConfigType.class);
        Object currentConfig;
        if (configType == ConfigType.SERVER) {
            currentConfig = ServerConfig.CODEC.decode(buf);
        } else {
            currentConfig = IndividualConfig.CODEC.decode(buf);
        }
        //int varInt = buf.readVarInt();
        return new SyncConfigC2SPayload(configType, currentConfig);
    }
}
