package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class VisitorNoItemPickupRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidVisitorNoItemPickup = true;

    private VisitorNoItemPickupRule() {
    }
}
