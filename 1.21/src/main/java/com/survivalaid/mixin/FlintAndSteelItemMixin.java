package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidTntLikeBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void survivalAid$primeClickedBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        if (!SurvivalAidTntLikeBlocks.prime(world, context.getBlockPos(), context.getPlayer())) {
            return;
        }
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            player.getStackInHand(context.getHand()).damage(1, player, player.getActiveHand());
            player.incrementStat(Stats.USED.getOrCreateItem(Items.FLINT_AND_STEEL));
        }
        cir.setReturnValue(ActionResult.success(world.isClient()));
    }
}
