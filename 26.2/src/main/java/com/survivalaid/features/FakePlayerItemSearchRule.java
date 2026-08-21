package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class FakePlayerItemSearchRule {
    public static final String SURVIVAL_AID_FAKE_TAG = "survivalaid_fake";

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidFakePlayerItemSearch = false;

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidFakePlayerScanAll = false;

    private FakePlayerItemSearchRule() {
    }
}
