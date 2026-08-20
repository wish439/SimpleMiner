package com.wishtoday.ts.simpleminer.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public record MineBlockSyncS2CPayload(int length, Set<BlockPos> pos) implements CustomPayload {
    public static final Id<MineBlockSyncS2CPayload> ID = new Id<>(Identifier.of("simpleminer", "mine_block_sync_payload"));
    private static final PacketCodec<PacketByteBuf, Set<BlockPos>> POS_CODEC = PacketCodecs.collection(HashSet::new, BlockPos.PACKET_CODEC);
    public static final PacketCodec<PacketByteBuf, MineBlockSyncS2CPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, MineBlockSyncS2CPayload::length, POS_CODEC, MineBlockSyncS2CPayload::pos, MineBlockSyncS2CPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
