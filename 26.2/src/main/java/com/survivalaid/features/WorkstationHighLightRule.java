package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class WorkstationHighLightRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidWorkstationHighLight = false;

    private WorkstationHighLightRule() {
    }
}
