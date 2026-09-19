package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class VillagerInstantLevelUpRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidVillagerInstantLevelUp = false;

    private VillagerInstantLevelUpRule() {
    }
}
