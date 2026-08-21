package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidTntLikeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/AbstractBlockMixin.class */
@Mixin({BlockBehaviour.class})
public abstract class AbstractBlockMixin {
    @Inject(method = {"neighborChanged"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeBlockOnRedstone(BlockState state, Level world, BlockPos pos, Block sourceBlock, Orientation orientation, boolean notify, CallbackInfo ci) {
        if (world.hasNeighborSignal(pos) && SurvivalAidTntLikeBlocks.prime(world, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = {"onPlace"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeAddedPoweredBlock(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.is(state.getBlock()) && world.hasNeighborSignal(pos) && SurvivalAidTntLikeBlocks.prime(world, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = {"onProjectileHit"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeBlockOnBurningProjectile(Level world, BlockState state, BlockHitResult hit, Projectile projectile, CallbackInfo ci) {
        if (!world.isClientSide() && projectile.isOnFire() && projectile.mayInteract((ServerLevel) world, hit.getBlockPos()) && SurvivalAidTntLikeBlocks.prime(world, hit.getBlockPos())) {
            ci.cancel();
        }
    }
}
