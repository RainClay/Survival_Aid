package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void survivalAid$ignoreConfiguredEntityCramming(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (source == entity.getWorld().getDamageSources().cramming() && SurvivalAidRules.ignoresEntityCramming(entity)) {
            cir.setReturnValue(false);
        }
    }
}
