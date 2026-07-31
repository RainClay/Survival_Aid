package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.BotMinecartPreserveRule;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.3-1.0.1.jar:com/survivalaid/mixin/BotMinecartPreserveMixin.class */
@Mixin({EntityPlayerMPFake.class})
public abstract class BotMinecartPreserveMixin {
    @Inject(method = {"shakeOff"}, at = {@At("HEAD")})
    private void survivalAid$dismountBeforeKill(CallbackInfo ci) {
        EntityPlayerMPFake self;
        class_1297 vehicle;
        if (BotMinecartPreserveRule.survivalAidBotMinecartPreserve && (vehicle = (self = (EntityPlayerMPFake) this).method_5854()) != null && !(vehicle instanceof class_1657)) {
            self.method_5848();
        }
    }
}
