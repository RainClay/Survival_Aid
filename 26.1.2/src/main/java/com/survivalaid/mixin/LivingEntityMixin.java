package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/LivingEntityMixin.class */
@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {
    @Inject(method = {"hurtServer"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$ignoreConfiguredEntityCramming(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (source == entity.damageSources().cramming() && SurvivalAidRules.ignoresEntityCramming(entity)) {
            cir.setReturnValue(false);
        }
    }
}
