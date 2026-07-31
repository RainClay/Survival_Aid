package com.survivalaid.mixin;

import com.survivalaid.features.DisableVillageCatSpawnRule;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_4274;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/mixin/CatSpawnerMixin.class */
@Mixin({class_4274.class})
public abstract class CatSpawnerMixin {
    @Inject(method = {"method_20263"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$disableVillageCatSpawn(class_3218 world, class_2338 pos, CallbackInfoReturnable<Integer> cir) {
        if (DisableVillageCatSpawnRule.survivalAidDisableVillageCatSpawn) {
            cir.setReturnValue(0);
        }
    }
}
