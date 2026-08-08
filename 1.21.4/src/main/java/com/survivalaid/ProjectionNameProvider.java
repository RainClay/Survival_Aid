package com.survivalaid;

import java.io.File;
import java.nio.file.Path;

public final class ProjectionNameProvider {
    private ProjectionNameProvider() {
    }

    public static String getCurrentProjectionFileName() {
        try {
            Class<?> dataManager = Class.forName("fi.dy.masa.litematica.data.DataManager");
            Object mgr = dataManager.getMethod("getSchematicPlacementManager").invoke(null);
            if (mgr == null) {
                return null;
            }
            Object placement = mgr.getClass().getMethod("getSelectedSchematicPlacement").invoke(mgr);
            if (placement == null) {
                return null;
            }
            Object file = placement.getClass().getMethod("getSchematicFile").invoke(placement);
            if (file instanceof File f) {
                return f.getName();
            }
            if (file instanceof Path p) {
                return p.getFileName().toString();
            }
            Object displayName = placement.getClass().getMethod("getName").invoke(placement);
            return displayName == null ? null : displayName.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
