package com.survivalaid;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SurvivalAidProjectionPayload(String name) implements CustomPayload {
    public static final CustomPayload.Id<SurvivalAidProjectionPayload> TYPE = new CustomPayload.Id<>(Identifier.of("survival_aid", "projection"));
    public static final PacketCodec<ByteBuf, SurvivalAidProjectionPayload> CODEC = PacketCodecs.STRING.xmap(SurvivalAidProjectionPayload::new, SurvivalAidProjectionPayload::name);

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}
