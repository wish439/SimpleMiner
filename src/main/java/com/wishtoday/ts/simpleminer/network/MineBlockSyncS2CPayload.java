package com.wishtoday.ts.simpleminer.network;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MineBlockSyncS2CPayload(int length) implements CustomPayload {
    public static final Id<MineBlockSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "mine_block_sync_payload"));
    public static final PacketCodec<PacketByteBuf, MineBlockSyncS2CPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, MineBlockSyncS2CPayload::length, MineBlockSyncS2CPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
