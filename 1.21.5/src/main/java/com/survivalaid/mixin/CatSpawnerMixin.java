package com.survivalaid.mixin;

import com.survivalaid.features.DisableVillageCatSpawnRule;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CatSpawner.class)
public abstract class CatSpawnerMixin {
    @Inject(method = "spawnInHouse", at = @At("HEAD"), cancellable = true)
    private void survivalAid$disableVillageCatSpawn(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (DisableVillageCatSpawnRule.survivalAidDisableVillageCatSpawn) {
            ci.cancel();
        }
    }
}
