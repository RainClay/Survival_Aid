package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.SurvivalAidRules;
import com.survivalaid.features.BotMinecartPreserveRule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public abstract class EntityMixin {
    @Inject(method = {"push"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$skipConfiguredSameTypeStackingPush(Entity otherEntity, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (SurvivalAidRules.skipsStackingPush(entity, otherEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = {"remove"}, at = {@At("HEAD")})
    private void survivalAid$dismountBotBeforeRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity vehicle;
        if (!BotMinecartPreserveRule.survivalAidBotMinecartPreserve) {
            return;
        }
        Entity self = (Entity) (Object) this;
        if ((self instanceof EntityPlayerMPFake) && (vehicle = self.getVehicle()) != null && !(vehicle instanceof Player)) {
            self.stopRiding();
        }
    }
}
