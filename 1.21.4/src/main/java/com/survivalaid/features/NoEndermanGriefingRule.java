package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class NoEndermanGriefingRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidNoEndermanGriefing = false;

    private NoEndermanGriefingRule() {
    }
}
