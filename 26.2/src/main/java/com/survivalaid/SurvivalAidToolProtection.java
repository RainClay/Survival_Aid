package com.survivalaid;

import com.survivalaid.features.PreventToolBreakRule;
import com.survivalaid.features.PreventToolBreakThresholdRule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidToolProtection.class */
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
        player.sendSystemMessage(Component.literal("耐久过低，已阻止使用：" + stack.getItemName().getString() + " 剩余 " + remainingDurability + " 点耐久。"));
        return true;
    }
}
