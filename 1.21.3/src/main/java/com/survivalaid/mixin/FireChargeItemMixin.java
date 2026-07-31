package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidTntLikeBlocks;
import net.minecraft.class_1269;
import net.minecraft.class_1778;
import net.minecraft.class_1799;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_3468;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.3-1.0.1.jar:com/survivalaid/mixin/FireChargeItemMixin.class */
@Mixin({class_1778.class})
public abstract class FireChargeItemMixin {
    @Inject(method = {"method_7884"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeClickedBlock(class_1838 context, CallbackInfoReturnable<class_1269> cir) {
        class_1937 world = context.method_8045();
        if (!SurvivalAidTntLikeBlocks.prime(world, context.method_8037(), context.method_8036())) {
            return;
        }
        class_1799 stack = context.method_8041();
        if (context.method_8036() != null) {
            stack.method_57008(1, context.method_8036());
            context.method_8036().method_7259(class_3468.field_15372.method_14956(stack.method_7909()));
        }
        cir.setReturnValue(world.method_8608() ? class_1269.field_5812 : class_1269.field_21466);
    }
}
