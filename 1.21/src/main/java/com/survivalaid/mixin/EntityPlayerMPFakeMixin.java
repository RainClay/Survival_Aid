package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.FakePlayerItemSearchRule;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMPFake.class)
public abstract class EntityPlayerMPFakeMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void survivalAid$tagFakePlayer(CallbackInfo ci) {
        if (!FakePlayerItemSearchRule.survivalAidFakePlayerScanAll) {
            ((ServerPlayerEntity) (Object) this).addCommandTag(FakePlayerItemSearchRule.SURVIVAL_AID_FAKE_TAG);
        }
    }
}
