package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class InstantHopperRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidInstantHopper = false;

    private InstantHopperRule() {
    }
}
