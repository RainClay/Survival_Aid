package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class LowHealthGlowThresholdRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static int survivalAidLowHealthGlowThreshold = 6;

    private LowHealthGlowThresholdRule() {
    }
}
