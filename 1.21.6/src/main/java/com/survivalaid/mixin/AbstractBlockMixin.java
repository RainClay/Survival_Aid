package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidTntLikeBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    private void survivalAid$primeBlockOnRedstone(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (world.isReceivingRedstonePower(pos) && SurvivalAidTntLikeBlocks.prime(world, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    private void survivalAid$primeAddedPoweredBlock(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!oldState.isOf(state.getBlock()) && world.isReceivingRedstonePower(pos) && SurvivalAidTntLikeBlocks.prime(world, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void survivalAid$primeBlockOnBurningProjectile(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        if (!world.isClient() && projectile.isOnFire() && projectile.canModifyAt((ServerWorld) world, hit.getBlockPos()) && SurvivalAidTntLikeBlocks.prime(world, hit.getBlockPos())) {
            ci.cancel();
        }
    }
}
