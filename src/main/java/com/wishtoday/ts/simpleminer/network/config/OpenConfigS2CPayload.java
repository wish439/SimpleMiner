package com.wishtoday.ts.simpleminer.network.config;

import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenConfigS2CPayload(ConfigType type, Object currentConfig) implements CustomPayload {
    public static final CustomPayload.Id<OpenConfigS2CPayload> ID = new Id<>(Identifier.of("simpleminer","openconfig_s2c"));
    public static final PacketCodec<PacketByteBuf, OpenConfigS2CPayload> CODEC = PacketCodec.of(
            OpenConfigS2CPayload::encode, OpenConfigS2CPayload::decode);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static void encode(OpenConfigS2CPayload value, PacketByteBuf buf) {
        buf.writeEnumConstant(value.type);
        if (value.type == ConfigType.SERVER) {
            ServerConfig.CODEC.encode(buf, (ServerConfig) value.currentConfig);
        }
        if (value.type == ConfigType.INDIVIDUAL) {
            IndividualConfig.CODEC.encode(buf, (IndividualConfig) value.currentConfig);
        }
        //buf.writeVarInt(value.syncId);
    }

    private static OpenConfigS2CPayload decode(PacketByteBuf buf) {
        ConfigType configType = buf.readEnumConstant(ConfigType.class);
        Object currentConfig;
        if (configType == ConfigType.SERVER) {
           currentConfig = ServerConfig.CODEC.decode(buf);
        } else {
           currentConfig = IndividualConfig.CODEC.decode(buf);
        }
        //int varInt = buf.readVarInt();
        return new OpenConfigS2CPayload(configType, currentConfig);
    }
}
