package com.survivalaid.mixin;

import com.survivalaid.features.InstantHopperRule;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Accessor("transferCooldown")
    abstract void survivalAid$setTransferCooldown(int value);

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void survivalAid$instantHopper(World world, BlockPos pos, BlockState state, HopperBlockEntity hopper, CallbackInfo ci) {
        if (InstantHopperRule.survivalAidInstantHopper) {
            ((HopperBlockEntityMixin) (Object) hopper).survivalAid$setTransferCooldown(0);
        }
    }
}
