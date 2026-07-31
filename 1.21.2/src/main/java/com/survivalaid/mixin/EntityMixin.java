package com.survivalaid.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.SurvivalAidRules;
import com.survivalaid.features.BotMinecartPreserveRule;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.2-1.0.1.jar:com/survivalaid/mixin/EntityMixin.class */
@Mixin({class_1297.class})
public abstract class EntityMixin {
    @Inject(method = {"method_5697"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$skipConfiguredSameTypeStackingPush(class_1297 otherEntity, CallbackInfo ci) {
        class_1297 entity = (class_1297) this;
        if (SurvivalAidRules.skipsStackingPush(entity, otherEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = {"method_5650"}, at = {@At("HEAD")})
    private void survivalAid$dismountBotBeforeRemove(class_1297.class_5529 reason, CallbackInfo ci) {
        class_1297 vehicle;
        if (!BotMinecartPreserveRule.survivalAidBotMinecartPreserve) {
            return;
        }
        class_1297 self = (class_1297) this;
        if ((self instanceof EntityPlayerMPFake) && (vehicle = self.method_5854()) != null && !(vehicle instanceof class_1657)) {
            self.method_5848();
        }
    }
}
