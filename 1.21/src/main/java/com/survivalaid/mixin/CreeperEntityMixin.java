package com.survivalaid.mixin;

import com.survivalaid.features.CreeperGriefingControlRule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
    @Redirect(method = "createExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"))
    private Explosion survivalAid$controlCreeperBlockDamage(World world, Entity entity, double x, double y, double z, float power, boolean causeFire, DestructionType destructionType) {
        if (CreeperGriefingControlRule.survivalAidCreeperGriefingControl) {
            return world.createExplosion(entity, x, y, z, power, causeFire, DestructionType.KEEP);
        }
        return world.createExplosion(entity, x, y, z, power, causeFire, sourceType);
    }
}
