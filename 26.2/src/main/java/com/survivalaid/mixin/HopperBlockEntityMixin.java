package com.survivalaid.mixin;

import com.survivalaid.features.InstantHopperRule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Accessor("cooldownTime")
    abstract void survivalAid$setCooldownTime(int value);

    @Inject(method = "pushItemsTick", at = @At("HEAD"))
    private static void survivalAid$instantHopper(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, CallbackInfo ci) {
        if (InstantHopperRule.survivalAidInstantHopper) {
            ((HopperBlockEntityMixin) (Object) hopper).survivalAid$setCooldownTime(0);
        }
    }
}
