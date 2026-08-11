package com.survivalaid;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SearchItemRequestPayload(String name) implements CustomPayload {
    public static final CustomPayload.Id<SearchItemRequestPayload> TYPE = new CustomPayload.Id<>(Identifier.of("survival_aid", "searchitem_request"));
    public static final PacketCodec<ByteBuf, SearchItemRequestPayload> CODEC = PacketCodecs.STRING.xmap(SearchItemRequestPayload::new, SearchItemRequestPayload::name);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
