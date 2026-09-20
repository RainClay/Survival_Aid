package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class DeathCoordinateMessageRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidDeathCoordinateMessage = false;

    private DeathCoordinateMessageRule() {
    }
}
