package com.survivalaid;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SearchItemResultPayload(String itemId) implements CustomPayload {
    public static final CustomPayload.Id<SearchItemResultPayload> TYPE = new CustomPayload.Id<>(Identifier.of("survival_aid", "searchitem_result"));
    public static final PacketCodec<ByteBuf, SearchItemResultPayload> CODEC = PacketCodecs.STRING.xmap(SearchItemResultPayload::new, SearchItemResultPayload::itemId);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
