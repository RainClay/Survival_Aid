package com.survivalaid;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SearchItemResultPayload(String itemId) implements CustomPacketPayload {
    public static final Type<SearchItemResultPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("survival_aid", "searchitem_result"));
    public static final StreamCodec<ByteBuf, SearchItemResultPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(SearchItemResultPayload::new, SearchItemResultPayload::itemId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
