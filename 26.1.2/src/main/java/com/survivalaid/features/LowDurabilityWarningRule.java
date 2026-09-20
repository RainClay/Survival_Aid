package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class LowDurabilityWarningRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static int survivalAidLowDurabilityWarning = 0;

    private LowDurabilityWarningRule() {
    }
}
