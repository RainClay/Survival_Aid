package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class AutoEatFoodThresholdRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static int survivalAidAutoEatFoodThreshold = 12;

    private AutoEatFoodThresholdRule() {
    }
}
