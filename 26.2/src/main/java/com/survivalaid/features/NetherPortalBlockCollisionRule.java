package com.survivalaid.features;

import carpet.api.settings.Rule;

public final class NetherPortalBlockCollisionRule {

    @Rule(categories = {"survival", "survival_aid"})
    public static boolean survivalAidNetherPortalSolid = false;

    private NetherPortalBlockCollisionRule() {
    }
}
