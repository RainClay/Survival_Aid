package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidTntLikeBlocks;
import net.minecraft.class_1676;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3965;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.5-1.0.1.jar:com/survivalaid/mixin/AbstractBlockMixin.class */
@Mixin({class_4970.class})
public abstract class AbstractBlockMixin {
    @Inject(method = {"method_9612"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeBlockOnRedstone(class_2680 state, class_1937 world, class_2338 pos, class_2248 sourceBlock, class_2338 sourcePos, boolean notify, CallbackInfo ci) {
        if (world.method_49803(pos) && SurvivalAidTntLikeBlocks.prime(world, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = {"method_9615"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeAddedPoweredBlock(class_2680 state, class_1937 world, class_2338 pos, class_2680 oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.method_27852(state.method_26204()) && world.method_49803(pos) && SurvivalAidTntLikeBlocks.prime(world, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = {"method_19286"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeBlockOnBurningProjectile(class_1937 world, class_2680 state, class_3965 hit, class_1676 projectile, CallbackInfo ci) {
        if (!world.method_8608() && projectile.method_5809() && projectile.method_36971((class_3218) world, hit.method_17777()) && SurvivalAidTntLikeBlocks.prime(world, hit.method_17777())) {
            ci.cancel();
        }
    }
}
