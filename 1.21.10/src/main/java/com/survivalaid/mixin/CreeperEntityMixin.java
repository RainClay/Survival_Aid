package com.survivalaid.mixin;

import com.survivalaid.features.CreeperGriefingControlRule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @Redirect(method = "createExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)V"))
    private void survivalAid$controlCreeperBlockDamage(World world, Entity entity, double x, double y, double z, float power, boolean causeFire, ExplosionSourceType sourceType) {
        world.createExplosion(entity, x, y, z, power, causeFire, CreeperGriefingControlRule.survivalAidCreeperGriefingControl ? ExplosionSourceType.NONE : sourceType);
    }
}
