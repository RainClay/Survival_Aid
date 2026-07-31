package com.survivalaid.mixin;

import com.survivalaid.features.NoEndermanGriefingRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.11-1.0.1.jar:com/survivalaid/mixin/EndermanPickUpBlockGoalMixin.class */
@Mixin(targets = {"net/minecraft/class_1560$class_1563"})
public abstract class EndermanPickUpBlockGoalMixin {
    @Inject(method = {"method_6264"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$disableEndermanPickUpBlock(CallbackInfoReturnable<Boolean> cir) {
        if (NoEndermanGriefingRule.survivalAidNoEndermanGriefing) {
            cir.setReturnValue(false);
        }
    }
}
