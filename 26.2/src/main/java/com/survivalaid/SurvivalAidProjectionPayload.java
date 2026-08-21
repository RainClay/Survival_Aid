package com.survivalaid;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SurvivalAidProjectionPayload(String name) implements CustomPacketPayload {
    public static final Type<SurvivalAidProjectionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("survival_aid", "projection"));
    public static final StreamCodec<ByteBuf, SurvivalAidProjectionPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(SurvivalAidProjectionPayload::new, SurvivalAidProjectionPayload::name);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
