package com.survivalaid.mixin;

import com.survivalaid.features.NetherPortalBlockCollisionRule;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    @Inject(method = "getInsideCollisionShape", at = @At("HEAD"), cancellable = true)
    private void survivalAid$fullPortalRange(BlockState state, BlockView world, BlockPos pos, Entity entity, CallbackInfoReturnable<VoxelShape> cir) {
        if (NetherPortalBlockCollisionRule.survivalAidNetherPortalSolid) {
            cir.setReturnValue(VoxelShapes.fullCube());
        }
    }
}
