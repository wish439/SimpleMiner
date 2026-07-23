package com.wishtoday.ts.simpleminer.undo.network.payloads;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TakeItemSyncC2SPayload(ItemStackKey key, int amount, int syncId) implements CustomPayload{
    public static final CustomPayload.Id<TakeItemSyncC2SPayload> ID = new Id<>(Identifier.of("simpleminer", "take_item_sync_c2s_payload"));
    public static final PacketCodec<RegistryByteBuf, TakeItemSyncC2SPayload> CODEC = PacketCodec.tuple(ItemStackKey.PACKET_CODEC, TakeItemSyncC2SPayload::key, PacketCodecs.INTEGER, TakeItemSyncC2SPayload::amount, PacketCodecs.INTEGER, TakeItemSyncC2SPayload::syncId, TakeItemSyncC2SPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
