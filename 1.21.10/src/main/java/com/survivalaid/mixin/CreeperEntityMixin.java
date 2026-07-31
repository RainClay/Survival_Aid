package com.survivalaid.mixin;

import com.survivalaid.features.CreeperGriefingControlRule;
import net.minecraft.class_1548;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.10-1.0.1.jar:com/survivalaid/mixin/CreeperEntityMixin.class */
@Mixin({class_1548.class})
public abstract class CreeperEntityMixin {
    @ModifyArg(method = {"method_7006"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3218;method_8437(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_1937$class_7867;)V"), index = 5)
    private class_1937.class_7867 survivalAid$controlCreeperBlockDamage(class_1937.class_7867 sourceType) {
        if (CreeperGriefingControlRule.survivalAidCreeperGriefingControl) {
            return class_1937.class_7867.field_40888;
        }
        return sourceType;
    }
}
