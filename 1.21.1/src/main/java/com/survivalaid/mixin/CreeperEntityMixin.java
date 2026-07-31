package com.survivalaid.mixin;

import com.survivalaid.features.CreeperGriefingControlRule;
import net.minecraft.class_1297;
import net.minecraft.class_1548;
import net.minecraft.class_1927;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/mixin/CreeperEntityMixin.class */
@Mixin({class_1548.class})
public abstract class CreeperEntityMixin {
    @Redirect(method = {"method_7006"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1937;method_8437(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_1937$class_7867;)Lnet/minecraft/class_1927;"))
    private class_1927 survivalAid$controlCreeperBlockDamage(class_1937 world, class_1297 entity, double x, double y, double z, float power, class_1937.class_7867 sourceType) {
        if (CreeperGriefingControlRule.survivalAidCreeperGriefingControl) {
            return world.method_8437(entity, x, y, z, power, class_1937.class_7867.field_40888);
        }
        return world.method_8437(entity, x, y, z, power, sourceType);
    }
}
