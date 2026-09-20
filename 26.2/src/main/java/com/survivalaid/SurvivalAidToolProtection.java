package com.survivalaid;

import com.survivalaid.features.PreventToolBreakRule;
import com.survivalaid.features.PreventToolBreakThresholdRule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class SurvivalAidToolProtection {
    private SurvivalAidToolProtection() {
    }

    public static boolean shouldBlock(ServerPlayer player, ItemStack stack) {
        if (!PreventToolBreakRule.survivalAidPreventToolBreak || stack == null || stack.isEmpty() || !stack.isDamageableItem()) {
            return false;
        }
        int threshold = Math.max(0, PreventToolBreakThresholdRule.survivalAidPreventToolBreakThreshold);
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
        if (remainingDurability > threshold) {
            return false;
        }
        player.sendSystemMessage(Component.translatable("survival_aid.message.tool_blocked", stack.getItemName().getString(), remainingDurability));
        return true;
    }
}
