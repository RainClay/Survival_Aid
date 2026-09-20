package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class NoItemDespawnRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidNoItemDespawn = false;

    private NoItemDespawnRule() {
    }
}
