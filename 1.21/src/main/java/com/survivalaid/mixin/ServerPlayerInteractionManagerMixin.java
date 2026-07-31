package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidToolProtection;
import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_3222;
import net.minecraft.class_3225;
import net.minecraft.class_3965;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/mixin/ServerPlayerInteractionManagerMixin.class */
@Mixin({class_3225.class})
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected class_3222 field_14008;

    @Inject(method = {"method_14263"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorBlockBreaking(class_2338 pos, class_2846.class_2847 action, class_2350 direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.field_14008)) {
            SurvivalAidVisitors.notifyBlocked(this.field_14008);
            ci.cancel();
        }
    }

    @Inject(method = {"method_14266"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorTryBreakBlock(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
        if (SurvivalAidToolProtection.shouldBlock(this.field_14008, this.field_14008.method_6047())) {
            cir.setReturnValue(false);
        } else if (SurvivalAidVisitors.isVisitor(this.field_14008)) {
            SurvivalAidVisitors.notifyBlocked(this.field_14008);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"method_14256"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorItemInteraction(class_3222 player, class_1937 world, class_1799 stack, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        if (SurvivalAidToolProtection.shouldBlock(player, stack)) {
            cir.setReturnValue(class_1269.field_5814);
        } else if (SurvivalAidVisitors.isVisitor(player)) {
            SurvivalAidVisitors.notifyBlocked(player);
            cir.setReturnValue(class_1269.field_5814);
        }
    }

    @Inject(method = {"method_14262"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorBlockInteraction(class_3222 player, class_1937 world, class_1799 stack, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
        if (SurvivalAidToolProtection.shouldBlock(player, stack)) {
            cir.setReturnValue(class_1269.field_5814);
        } else if (SurvivalAidVisitors.isVisitor(player)) {
            SurvivalAidVisitors.notifyBlocked(player);
            cir.setReturnValue(class_1269.field_5814);
        }
    }
}
