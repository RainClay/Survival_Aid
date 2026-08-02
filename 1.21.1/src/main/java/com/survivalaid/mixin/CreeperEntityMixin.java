package com.survivalaid.mixin;

import com.survivalaid.features.CreeperGriefingControlRule;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World.ExplosionSourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private ExplosionSourceType survivalAid$controlCreeperBlockDamage(ExplosionSourceType sourceType) {
        return CreeperGriefingControlRule.survivalAidCreeperGriefingControl ? ExplosionSourceType.NONE : sourceType;
    }
}
