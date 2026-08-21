package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidVisitors;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/ServerPlayNetworkHandlerMixin.class */
@Mixin({ServerGamePacketListenerImpl.class})
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = {"handleInteract"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorEntityInteraction(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyBlocked(this.player);
            ci.cancel();
        }
    }

    @Inject(method = {"handleChatCommand"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$blockVisitorCommands(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        if (SurvivalAidVisitors.isVisitor(this.player)) {
            SurvivalAidVisitors.notifyCommandBlocked(this.player);
            ci.cancel();
        }
    }
}
