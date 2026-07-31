package com.survivalaid.mixin;

import com.survivalaid.features.NoEndermanGriefingRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.6-1.0.1.jar:com/survivalaid/mixin/EndermanPlaceBlockGoalMixin.class */
@Mixin(targets = {"net/minecraft/class_1560$class_1561"})
public abstract class EndermanPlaceBlockGoalMixin {
    @Inject(method = {"method_6264"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$disableEndermanPlaceBlock(CallbackInfoReturnable<Boolean> cir) {
        if (NoEndermanGriefingRule.survivalAidNoEndermanGriefing) {
            cir.setReturnValue(false);
        }
    }
}
