package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class VoidPlayerRescueRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidVoidPlayerRescue = false;

    private VoidPlayerRescueRule() {
    }
}
