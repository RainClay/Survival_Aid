package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.BotMinecartPreserveRule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/BotMinecartPreserveMixin.class */
@Mixin({EntityPlayerMPFake.class})
public abstract class BotMinecartPreserveMixin {
    @Inject(method = {"shakeOff"}, at = {@At("HEAD")})
    private void survivalAid$dismountBeforeKill(CallbackInfo ci) {
        EntityPlayerMPFake self;
        Entity vehicle;
        if (BotMinecartPreserveRule.survivalAidBotMinecartPreserve && (vehicle = (self = (EntityPlayerMPFake) this).getVehicle()) != null && !(vehicle instanceof Player)) {
            self.stopRiding();
        }
    }
}
