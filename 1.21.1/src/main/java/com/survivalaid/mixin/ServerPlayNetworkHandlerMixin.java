package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.class_2824;
import net.minecraft.class_3222;
import net.minecraft.class_3244;
import net.minecraft.class_7472;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/mixin/ServerPlayNetworkHandlerMixin.class */
@Mixin({class_3244.class})
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public class_3222 field_14140;

    @Inject(method = {"method_12062"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorEntityInteraction(class_2824 packet, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.field_14140)) {
            SurvivalAidVisitors.notifyBlocked(this.field_14140);
            ci.cancel();
        }
    }

    @Inject(method = {"method_43667"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorCommands(class_7472 packet, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.field_14140)) {
            SurvivalAidVisitors.notifyCommandBlocked(this.field_14140);
            ci.cancel();
        }
    }
}
