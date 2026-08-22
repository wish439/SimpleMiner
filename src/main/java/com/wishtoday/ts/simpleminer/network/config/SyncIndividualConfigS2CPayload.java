package com.wishtoday.ts.simpleminer.network.config;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncIndividualConfigS2CPayload(IndividualConfig config) implements CustomPayload {
    public static final CustomPayload.Id<SyncIndividualConfigS2CPayload> ID = new Id<>(Identifier.of("simpleminer","sync_individual_config_s2c"));
    public static final PacketCodec<PacketByteBuf, SyncIndividualConfigS2CPayload> CODEC = PacketCodec.tuple(IndividualConfig.CODEC, SyncIndividualConfigS2CPayload::config, SyncIndividualConfigS2CPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
