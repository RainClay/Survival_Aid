package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    @Final
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorEntityInteraction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(player)) {
            SurvivalAidVisitors.notifyBlocked(player);
            ci.cancel();
        }
    }

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void survivalAid$blockVisitorCommands(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(player)) {
            SurvivalAidVisitors.notifyCommandBlocked(player);
            ci.cancel();
        }
    }
}
