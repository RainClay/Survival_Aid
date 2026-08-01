package com.survivalaid.mixin;

import com.survivalaid.features.NoEndermanGriefingRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/entity/mob/EndermanEntity$PickUpBlockGoal")
public abstract class EndermanPickUpBlockGoalMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void survivalAid$disableEndermanPickUpBlock(CallbackInfoReturnable<Boolean> cir) {
        if (NoEndermanGriefingRule.survivalAidNoEndermanGriefing) {
            cir.setReturnValue(false);
        }
    }
}
