package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidToolProtection;
import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorEntityInteraction(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self instanceof ServerPlayerEntity player) {
            if (SurvivalAidToolProtection.shouldBlock(player, player.getStackInHand(hand))) {
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
        }
        if (self instanceof ServerPlayerEntity player2) {
            if (SurvivalAidVisitors.isVisitor(player2)) {
                SurvivalAidVisitors.notifyBlocked(player2);
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorEntityAttack(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self instanceof ServerPlayerEntity player) {
            if (SurvivalAidToolProtection.shouldBlock(player, player.getMainHandStack())) {
                ci.cancel();
                return;
            }
        }
        if (self instanceof ServerPlayerEntity player2) {
            if (SurvivalAidVisitors.isVisitor(player2)) {
                SurvivalAidVisitors.notifyBlocked(player2);
                ci.cancel();
            }
        }
    }
}
