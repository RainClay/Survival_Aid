package com.survivalaid;

import com.survivalaid.features.PreventToolBreakRule;
import com.survivalaid.features.PreventToolBreakThresholdRule;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.3-1.0.1.jar:com/survivalaid/SurvivalAidToolProtection.class */
public final class SurvivalAidToolProtection {
    private SurvivalAidToolProtection() {
    }

    public static boolean shouldBlock(class_3222 player, class_1799 stack) {
        if (!PreventToolBreakRule.survivalAidPreventToolBreak || stack == null || stack.method_7960() || !stack.method_7963()) {
            return false;
        }
        int threshold = Math.max(0, PreventToolBreakThresholdRule.survivalAidPreventToolBreakThreshold);
        int remainingDurability = stack.method_7936() - stack.method_7919();
        if (remainingDurability > threshold) {
            return false;
        }
        player.method_7353(class_2561.method_43470("耐久过低，已阻止使用：" + stack.method_7964().getString() + " 剩余 " + remainingDurability + " 点耐久。"), true);
        return true;
    }
}
