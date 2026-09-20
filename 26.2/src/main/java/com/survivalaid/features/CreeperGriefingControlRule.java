package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class CreeperGriefingControlRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidCreeperGriefingControl = false;

    private CreeperGriefingControlRule() {
    }
}
