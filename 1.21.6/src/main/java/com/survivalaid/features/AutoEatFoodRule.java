package com.survivalaid.features;

import carpet.api.settings.Rule;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.6-1.0.1.jar:com/survivalaid/features/AutoEatFoodRule.class */
public final class AutoEatFoodRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidAutoEatFood = false;

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidAutoEatFoodDebug = false;

    private AutoEatFoodRule() {
    }
}
