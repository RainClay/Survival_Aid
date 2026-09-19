package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRuntime;
import com.survivalaid.features.WorkstationHighLightRule;
import com.survivalaid.features.ZombieFrightenGolemRule;
import com.survivalaid.features.VillagerInstantLevelUpRule;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

    @Accessor("levelingUp")
    public abstract boolean survivalAid$isLevelingUp();

    @Accessor("levelingUp")
    public abstract void survivalAid$setLevelingUp(boolean levelingUp);

    @Accessor("levelUpTimer")
    public abstract void survivalAid$setLevelUpTimer(int timer);

    @Invoker
    public abstract void invokeLevelUp();

    @Invoker
    public abstract void invokeSendOffersToCustomer();

    @Inject(method = "afterUsing(Lnet/minecraft/village/TradeOffer;)V", at = @At("TAIL"))
    private void survivalAid$instantVillagerLevelUp(net.minecraft.village.TradeOffer offer, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!VillagerInstantLevelUpRule.survivalAidVillagerInstantLevelUp) {
            return;
        }
        VillagerEntity self = (VillagerEntity) (Object) this;
        if (!survivalAid$isLevelingUp() || self.getCustomer() == null) {
            return;
        }
        invokeLevelUp();
        self.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.REGENERATION, 200, 0));

        survivalAid$setLevelingUp(false);
        survivalAid$setLevelUpTimer(0);
        invokeSendOffersToCustomer();
    }

    @Invoker
    public abstract boolean invokeHasRecentlySlept(long time);

    @Redirect(
            method = "canSummonGolem(J)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;hasRecentlySlept(J)Z")
    )
    private boolean survivalAid$frightenGolemIgnoreSleep(VillagerEntity self, long time) {
        return invokeHasRecentlySlept(time) || ZombieFrightenGolemRule.survivalAidZombieFrightenGolem;
    }
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void survivalAid$highlightWorkstation(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!WorkstationHighLightRule.survivalAidWorkstationHighLight || !player.isCreative()) {
            return;
        }
        VillagerEntity villager = (VillagerEntity) (Object) this;
        if (!(villager.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        Optional<GlobalPos> jobSite = villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE);
        if (jobSite.isEmpty()) {
            return;
        }
        BlockPos workstation = jobSite.get().pos();
        BlockState blockState = serverWorld.getBlockState(workstation);
        DisplayEntity.BlockDisplayEntity display = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, serverWorld);
        display.refreshPositionAfterTeleport(workstation.getX() + 0.5, workstation.getY() + 1.5, workstation.getZ() + 0.5);
        ((BlockDisplayEntityAccessor) display).survivalAid$setBlockState(blockState);
        display.setInvisible(true);
        display.setNoGravity(true);
        display.setGlowing(true);
        serverWorld.spawnEntity(display);
        SurvivalAidRuntime.trackWorkstationHighlight(display);
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
