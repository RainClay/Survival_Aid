package com.survivalaid;

import carpet.api.settings.SettingsManager;
import com.survivalaid.features.AutoEatFoodRule;
import com.survivalaid.features.AutoEatFoodThresholdRule;
import com.survivalaid.features.BotMinecartPreserveRule;
import com.survivalaid.features.CreeperGriefingControlRule;
import com.survivalaid.features.DeathCoordinateMessageRule;
import com.survivalaid.features.DisableVillageCatSpawnRule;
import com.survivalaid.features.InstantItemPickupRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.LowDurabilityWarningRule;
import com.survivalaid.features.LowHealthGlowRule;
import com.survivalaid.features.LowHealthGlowThresholdRule;
import com.survivalaid.features.NetherPortalBlockCollisionRule;
import com.survivalaid.features.NoCrammingEntitiesRule;
import com.survivalaid.features.NoEndermanGriefingRule;
import com.survivalaid.features.NoItemDespawnRule;
import com.survivalaid.features.PreventToolBreakRule;
import com.survivalaid.features.PreventToolBreakThresholdRule;
import com.survivalaid.features.StackingOptimizedEntitiesRule;
import com.survivalaid.features.TntLikeBlocksRule;
import com.survivalaid.features.VisitorNoItemPickupRule;
import com.survivalaid.features.VisitorPlayersRule;
import com.survivalaid.features.VoidPlayerRescueCooldownRule;
import com.survivalaid.features.VoidPlayerRescueRule;
import com.survivalaid.features.VoidPlayerRescueYRule;
import com.survivalaid.features.WorkstationHighLightRule;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/SurvivalAidSettings.class */
public final class SurvivalAidSettings {
    private static boolean extensionRegistered;
    private static boolean carpetRegistered;

    private SurvivalAidSettings() {
    }

    public static synchronized int registerRules() {
        if (extensionRegistered) {
            return SurvivalAidExtension.SETTINGS_MANAGER.getCarpetRules().size();
        }
        parseRulesInto(SurvivalAidExtension.SETTINGS_MANAGER);
        extensionRegistered = true;
        return SurvivalAidExtension.SETTINGS_MANAGER.getCarpetRules().size();
    }

    public static synchronized int registerCarpetRules(SettingsManager settingsManager) {
        if (settingsManager == null) {
            return 0;
        }
        if (carpetRegistered) {
            return settingsManager.getCarpetRules().size();
        }
        parseRulesInto(settingsManager);
        carpetRegistered = true;
        return settingsManager.getCarpetRules().size();
    }

    public static String getRuleNames() {
        return (String) SurvivalAidExtension.SETTINGS_MANAGER.getCarpetRules().stream().map(rule -> {
            return rule.name();
        }).sorted().collect(Collectors.joining(", "));
    }

    private static void parseRulesInto(SettingsManager settingsManager) {
        settingsManager.parseSettingsClass(CreeperGriefingControlRule.class);
        settingsManager.parseSettingsClass(DeathCoordinateMessageRule.class);
        settingsManager.parseSettingsClass(AutoEatFoodRule.class);
        settingsManager.parseSettingsClass(AutoEatFoodThresholdRule.class);
        settingsManager.parseSettingsClass(DisableVillageCatSpawnRule.class);
        settingsManager.parseSettingsClass(LowDurabilityWarningRule.class);
        settingsManager.parseSettingsClass(LowHealthGlowRule.class);
        settingsManager.parseSettingsClass(LowHealthGlowThresholdRule.class);
        settingsManager.parseSettingsClass(NoCrammingEntitiesRule.class);
        settingsManager.parseSettingsClass(NoEndermanGriefingRule.class);
        settingsManager.parseSettingsClass(InstantItemPickupRule.class);
        settingsManager.parseSettingsClass(ItemPickupFilterRule.class);
        settingsManager.parseSettingsClass(NoItemDespawnRule.class);
        settingsManager.parseSettingsClass(PreventToolBreakRule.class);
        settingsManager.parseSettingsClass(PreventToolBreakThresholdRule.class);
        settingsManager.parseSettingsClass(StackingOptimizedEntitiesRule.class);
        settingsManager.parseSettingsClass(TntLikeBlocksRule.class);
        settingsManager.parseSettingsClass(VisitorPlayersRule.class);
        settingsManager.parseSettingsClass(NetherPortalBlockCollisionRule.class);
        settingsManager.parseSettingsClass(VisitorNoItemPickupRule.class);
        settingsManager.parseSettingsClass(VoidPlayerRescueRule.class);
        settingsManager.parseSettingsClass(VoidPlayerRescueYRule.class);
        settingsManager.parseSettingsClass(VoidPlayerRescueCooldownRule.class);
        settingsManager.parseSettingsClass(WorkstationHighLightRule.class);
        settingsManager.parseSettingsClass(BotMinecartPreserveRule.class);
    }
}
