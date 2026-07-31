package com.survivalaid.features;

import carpet.api.settings.Rule;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/features/ItemPickupFilterRule.class */
public final class ItemPickupFilterRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static String survivalAidItemPickupFilter = "";

    @Rule(categories = {"survival", "survival_aid"})
    public static String survivalAidPlayerItemPickupWhitelist = "";

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidPlayerItemPickupWhitelistDebug = false;

    private ItemPickupFilterRule() {
    }
}
