package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class FakePlayerItemSearchRule {
    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidFakePlayerItemSearch = false;

    private FakePlayerItemSearchRule() {
    }
}
