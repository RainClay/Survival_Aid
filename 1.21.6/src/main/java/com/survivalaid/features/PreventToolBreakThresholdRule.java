package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class PreventToolBreakThresholdRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static int survivalAidPreventToolBreakThreshold = 10;

    private PreventToolBreakThresholdRule() {
    }
}
