package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRules;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.2-1.0.1.jar:com/survivalaid/mixin/LivingEntityMixin.class */
@Mixin({class_1309.class})
public abstract class LivingEntityMixin {
    @Inject(method = {"method_64397"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$ignoreConfiguredEntityCramming(class_1282 source, float amount, CallbackInfoReturnable<Boolean> cir) {
        class_1297 entity = (class_1297) this;
        if (source == entity.method_48923().method_48823() && SurvivalAidRules.ignoresEntityCramming(entity)) {
            cir.setReturnValue(false);
        }
    }
}
