package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidToolProtection;
import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorItemInteraction(int syncId, int slotId, int button, int actionType, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyBlocked(this.player);
            ci.cancel();
        }
    }

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (SurvivalAidToolProtection.shouldBlock(this.player, this.player.getMainHandStack())) {
            cir.setReturnValue(false);
        } else if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyBlocked(this.player);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorBlockInteraction(PlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (SurvivalAidToolProtection.shouldBlock(this.player, stack)) {
            cir.setReturnValue(ActionResult.PASS);
        } else if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyBlocked(this.player);
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
