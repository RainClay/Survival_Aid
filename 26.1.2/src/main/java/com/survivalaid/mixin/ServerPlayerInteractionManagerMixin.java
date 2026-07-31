package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidToolProtection;
import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/ServerPlayerInteractionManagerMixin.class */
@Mixin({ServerPlayerGameMode.class})
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = {"handleBlockBreakAction"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorBlockBreaking(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyBlocked(this.player);
            ci.cancel();
        }
    }

    @Inject(method = {"destroyBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (SurvivalAidToolProtection.shouldBlock(this.player, this.player.getMainHandItem())) {
            cir.setReturnValue(false);
        } else if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyBlocked(this.player);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"useItem"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorItemInteraction(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (SurvivalAidToolProtection.shouldBlock(player, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        } else if (SurvivalAidVisitors.isVisitor(player)) {
            SurvivalAidVisitors.notifyBlocked(player);
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = {"useItemOn"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorBlockInteraction(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (SurvivalAidToolProtection.shouldBlock(player, stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        } else if (SurvivalAidVisitors.isVisitor(player)) {
            SurvivalAidVisitors.notifyBlocked(player);
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
