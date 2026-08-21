package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidTntLikeBlocks;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/FireChargeItemMixin.class */
@Mixin({FireChargeItem.class})
public abstract class FireChargeItemMixin {
    @Inject(method = {"useOn"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$primeClickedBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = context.getLevel();
        if (!SurvivalAidTntLikeBlocks.prime(world, context.getClickedPos(), context.getPlayer())) {
            return;
        }
        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() != null) {
            stack.consume(1, context.getPlayer());
            context.getPlayer().awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
        cir.setReturnValue(world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }
}
