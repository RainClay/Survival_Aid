package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void survivalAid$ignoreConfiguredEntityCramming(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (source == entity.getDamageSources().cramming() && SurvivalAidRules.ignoresEntityCramming(entity)) {
            cir.setReturnValue(false);
        }
    }
}
