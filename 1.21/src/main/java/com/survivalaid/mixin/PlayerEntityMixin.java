package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidToolProtection;
import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/mixin/PlayerEntityMixin.class */
@Mixin({class_1657.class})
public abstract class PlayerEntityMixin {
    @Inject(method = {"method_7287"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorEntityInteraction(class_1297 entity, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        class_3222 class_3222Var = (class_1657) this;
        if (class_3222Var instanceof class_3222) {
            class_3222 player = class_3222Var;
            if (SurvivalAidToolProtection.shouldBlock(player, player.method_5998(hand))) {
                cir.setReturnValue(class_1269.field_5814);
                return;
            }
        }
        if (class_3222Var instanceof class_3222) {
            class_3222 player2 = class_3222Var;
            if (SurvivalAidVisitors.isVisitor(player2)) {
                SurvivalAidVisitors.notifyBlocked(player2);
                cir.setReturnValue(class_1269.field_5814);
            }
        }
    }

    @Inject(method = {"method_7324"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorEntityAttack(class_1297 target, CallbackInfo ci) {
        class_3222 class_3222Var = (class_1657) this;
        if (class_3222Var instanceof class_3222) {
            class_3222 player = class_3222Var;
            if (SurvivalAidToolProtection.shouldBlock(player, player.method_6047())) {
                ci.cancel();
                return;
            }
        }
        if (class_3222Var instanceof class_3222) {
            class_3222 player2 = class_3222Var;
            if (SurvivalAidVisitors.isVisitor(player2)) {
                SurvivalAidVisitors.notifyBlocked(player2);
                ci.cancel();
            }
        }
    }
}
