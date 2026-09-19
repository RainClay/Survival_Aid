package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRuntime;
import com.survivalaid.features.WorkstationHighLightRule;
import com.survivalaid.features.ZombieFrightenGolemRule;
import com.survivalaid.features.VillagerInstantLevelUpRule;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Villager.class})
public abstract class VillagerEntityMixin {

    @Accessor("increaseProfessionLevelOnUpdate")
    public abstract boolean survivalAid$willIncreaseOnUpdate();

    @Accessor("increaseProfessionLevelOnUpdate")
    public abstract void survivalAid$setIncreaseOnUpdate(boolean increase);

    @Accessor("updateMerchantTimer")
    public abstract void survivalAid$setMerchantTimer(int timer);

    @Invoker
    public abstract void invokeIncreaseMerchantCareer(net.minecraft.server.level.ServerLevel level);

    @Invoker
    public abstract void invokeResendOffersToTradingPlayer();

    @Inject(method = "rewardTradeXp(Lnet/minecraft/world/item/trading/MerchantOffer;)V", at = @At("TAIL"))
    private void survivalAid$instantVillagerLevelUp(net.minecraft.world.item.trading.MerchantOffer offer, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!VillagerInstantLevelUpRule.survivalAidVillagerInstantLevelUp) {
            return;
        }
        Villager self = (Villager) (Object) this;
        if (!survivalAid$willIncreaseOnUpdate() || self.getTradingPlayer() == null) {
            return;
        }
        if (self.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            invokeIncreaseMerchantCareer(serverLevel);
            self.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200, 0));
        }
        survivalAid$setIncreaseOnUpdate(false);
        survivalAid$setMerchantTimer(0);
        invokeResendOffersToTradingPlayer();
    }

    @Invoker
    public abstract boolean invokeGolemSpawnConditionsMet(long time);

    @Redirect(
            method = "wantsToSpawnGolem(J)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;golemSpawnConditionsMet(J)Z")
    )
    private boolean survivalAid$frightenGolemIgnoreSleep(Villager self, long time) {
        return invokeGolemSpawnConditionsMet(time) || ZombieFrightenGolemRule.survivalAidZombieFrightenGolem;
    }
    @Inject(method = {"mobInteract"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$highlightWorkstation(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!WorkstationHighLightRule.survivalAidWorkstationHighLight || !player.isShiftKeyDown()) {
            return;
        }
        Villager villager = (Villager) (Object) this;
        Level serverLevelLevel = villager.level();
        if (!(serverLevelLevel instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) serverLevelLevel;
        Optional<BlockPos> workstation = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).map(globalPos -> {
            return globalPos.pos();
        });
        if (workstation.isEmpty()) {
            return;
        }
        BlockPos pos = workstation.get();
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel);
        display.setPos(pos.getX(), pos.getY(), pos.getZ());
        display.setBlockState(serverLevel.getBlockState(pos));
        display.setGlowingTag(true);
        display.setNoGravity(true);
        display.setInvulnerable(true);
        display.setSilent(true);
        serverLevel.addFreshEntity(display);
        SurvivalAidRuntime.trackWorkstationHighlight(display);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
