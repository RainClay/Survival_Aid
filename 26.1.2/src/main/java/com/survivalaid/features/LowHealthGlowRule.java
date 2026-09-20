package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class LowHealthGlowRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidLowHealthGlow = false;

    private LowHealthGlowRule() {
    }
}
