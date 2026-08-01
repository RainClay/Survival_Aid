package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRules;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "collidesWith", at = @At("HEAD"), cancellable = true)
    private void survivalAid$skipConfiguredSameTypeStackingPush(Entity otherEntity, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (SurvivalAidRules.skipsStackingPush(entity, otherEntity)) {
            cir.setReturnValue(false);
        }
    }
}
