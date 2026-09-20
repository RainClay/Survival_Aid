package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class InstantItemPickupRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidInstantItemPickup = false;

    private InstantItemPickupRule() {
    }
}
