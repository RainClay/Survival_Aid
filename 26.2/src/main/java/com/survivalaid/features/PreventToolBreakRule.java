package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class PreventToolBreakRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidPreventToolBreak = false;

    private PreventToolBreakRule() {
    }
}
