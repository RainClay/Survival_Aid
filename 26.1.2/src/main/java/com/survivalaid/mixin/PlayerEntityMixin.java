package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidToolProtection;
import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/PlayerEntityMixin.class */
@Mixin({Player.class})
public abstract class PlayerEntityMixin {
    @Inject(method = {"interactOn"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorEntityInteraction(Entity entity, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
        ServerPlayer serverPlayer = (Player) this;
        if (serverPlayer instanceof ServerPlayer) {
            ServerPlayer player = serverPlayer;
            if (SurvivalAidToolProtection.shouldBlock(player, player.getItemInHand(hand))) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
        }
        if (serverPlayer instanceof ServerPlayer) {
            ServerPlayer player2 = serverPlayer;
            if (SurvivalAidVisitors.isVisitor(player2)) {
                SurvivalAidVisitors.notifyBlocked(player2);
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    @Inject(method = {"attack"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorEntityAttack(Entity target, CallbackInfo ci) {
        ServerPlayer serverPlayer = (Player) this;
        if (serverPlayer instanceof ServerPlayer) {
            ServerPlayer player = serverPlayer;
            if (SurvivalAidToolProtection.shouldBlock(player, player.getMainHandItem())) {
                ci.cancel();
                return;
            }
        }
        if (serverPlayer instanceof ServerPlayer) {
            ServerPlayer player2 = serverPlayer;
            if (SurvivalAidVisitors.isVisitor(player2)) {
                SurvivalAidVisitors.notifyBlocked(player2);
                ci.cancel();
            }
        }
    }
}
