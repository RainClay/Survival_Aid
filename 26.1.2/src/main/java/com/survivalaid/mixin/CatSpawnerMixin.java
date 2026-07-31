package com.survivalaid.mixin;

import com.survivalaid.features.DisableVillageCatSpawnRule;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.CatSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/CatSpawnerMixin.class */
@Mixin({CatSpawner.class})
public abstract class CatSpawnerMixin {
    @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$disableVillageCatSpawn(ServerLevel world, boolean something, CallbackInfo ci) {
        if (DisableVillageCatSpawnRule.survivalAidDisableVillageCatSpawn) {
            ci.cancel();
        }
    }
}
