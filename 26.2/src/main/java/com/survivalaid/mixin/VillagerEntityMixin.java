package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRuntime;
import com.survivalaid.features.WorkstationHighLightRule;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/VillagerEntityMixin.class */
@Mixin({Villager.class})
public abstract class VillagerEntityMixin {
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
        Display.BlockDisplay display = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, serverLevel);
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
