package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.BotMinecartPreserveRule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMPFake.class)
public abstract class BotMinecartPreserveMixin {
    @Inject(method = "shakeOff", at = @At("HEAD"))
    private void survivalAid$dismountBeforeKill(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        Entity vehicle = self.getVehicle();
        if (BotMinecartPreserveRule.survivalAidBotMinecartPreserve && vehicle != null && !(vehicle instanceof AbstractMinecartEntity)) {
            self.stopRiding();
        }
    }
}
