package com.survivalaid.features;

import carpet.api.settings.Rule;

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
