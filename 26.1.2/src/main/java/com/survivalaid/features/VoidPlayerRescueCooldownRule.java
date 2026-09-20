package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class VoidPlayerRescueCooldownRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static int survivalAidVoidPlayerRescueCooldown = 40;

    private VoidPlayerRescueCooldownRule() {
    }
}
