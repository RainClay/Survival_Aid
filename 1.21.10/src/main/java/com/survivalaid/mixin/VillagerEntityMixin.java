package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRuntime;
import com.survivalaid.features.WorkstationHighLightRule;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
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
