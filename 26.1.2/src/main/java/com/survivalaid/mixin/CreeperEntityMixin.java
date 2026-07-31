package com.survivalaid.mixin;

import com.survivalaid.features.CreeperGriefingControlRule;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/CreeperEntityMixin.class */
@Mixin({Creeper.class})
public abstract class CreeperEntityMixin {
    @ModifyArg(method = {"explodeCreeper"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"), index = 5)
    private Level.ExplosionInteraction survivalAid$controlCreeperBlockDamage(Level.ExplosionInteraction sourceType) {
        if (CreeperGriefingControlRule.survivalAidCreeperGriefingControl) {
            return Level.ExplosionInteraction.NONE;
        }
        return sourceType;
    }
}
