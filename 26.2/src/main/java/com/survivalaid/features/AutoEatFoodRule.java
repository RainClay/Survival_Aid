package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class AutoEatFoodRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidAutoEatFood = false;

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidAutoEatFoodDebug = false;

    private AutoEatFoodRule() {
    }
}
