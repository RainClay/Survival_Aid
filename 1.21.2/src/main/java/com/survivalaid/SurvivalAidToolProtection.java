package com.survivalaid;

import com.survivalaid.features.PreventToolBreakRule;
import com.survivalaid.features.PreventToolBreakThresholdRule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class SurvivalAidToolProtection {
    private SurvivalAidToolProtection() {
    }

    public static boolean shouldBlock(PlayerEntity player, ItemStack stack) {
        if (!PreventToolBreakRule.survivalAidPreventToolBreak || stack == null || stack.isEmpty() || !stack.isDamageable()) {
            return false;
        }
        int threshold = Math.max(0, PreventToolBreakThresholdRule.survivalAidPreventToolBreakThreshold);
        int remainingDurability = stack.getMaxDamage() - stack.getDamage();
        if (remainingDurability > threshold) {
            return false;
        }
        player.sendMessage(Text.translatable("survival_aid.message.tool_blocked", stack.getName().getString(), remainingDurability), true);
        return true;
    }
}
