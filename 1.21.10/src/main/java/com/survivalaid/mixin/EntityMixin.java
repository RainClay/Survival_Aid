package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.SurvivalAidRules;
import com.survivalaid.features.BotMinecartPreserveRule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void survivalAid$skipConfiguredSameTypeStackingPush(Entity otherEntity, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (SurvivalAidRules.skipsStackingPush(entity, otherEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void survivalAid$dismountBotBeforeRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (BotMinecartPreserveRule.survivalAidBotMinecartPreserve && (((Object) self) instanceof EntityPlayerMPFake) && self.getVehicle() != null && !(self.getVehicle() instanceof PlayerEntity)) {
            self.stopRiding();
        }
    }
}
