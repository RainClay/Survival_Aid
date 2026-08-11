package com.survivalaid;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SearchItemRequestPayload(String name) implements CustomPacketPayload {
    public static final Type<SearchItemRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("survival_aid", "searchitem_request"));
    public static final StreamCodec<ByteBuf, SearchItemRequestPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(SearchItemRequestPayload::new, SearchItemRequestPayload::name);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
