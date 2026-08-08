package com.survivalaid;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;

public final class ProjectionNameProvider {
    private ProjectionNameProvider() {
    }

    public static ProjectionClientState getCurrentProjectionState() {
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
            String name = readFileName(placement);
            if (name == null || name.isEmpty()) {
                return null;
            }
            int[] origin = readOrigin(placement);
            int originX = origin[0];
            int originY = origin[1];
            int originZ = origin[2];
            String rangeType = "ALL";
            int minY = -1;
            int maxY = -1;
            try {
                Object layerRange = dataManager.getMethod("getRenderLayerRange").invoke(null);
                if (layerRange != null) {
                    String t = invokeStr(layerRange, new String[]{"getLayerRangeType"});
                    if (t != null && !t.isEmpty()) {
                        rangeType = t;
                    }
                    minY = invokeInt(layerRange, new String[]{"getLayerMin", "getMinLayer"}, -1);
                    maxY = invokeInt(layerRange, new String[]{"getLayerMax", "getMaxLayer"}, -1);
                }
            } catch (Exception ignore) {
                rangeType = "ALL";
                minY = -1;
                maxY = -1;
            }
            return new ProjectionClientState(name, rangeType, minY, maxY, originX, originY, originZ);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readFileName(Object placement) throws Exception {
        Object file = placement.getClass().getMethod("getSchematicFile").invoke(placement);
        if (file instanceof File f) {
            return f.getName();
        }
        if (file instanceof Path p) {
            return p.getFileName().toString();
        }
        Object displayName = placement.getClass().getMethod("getName").invoke(placement);
        return displayName == null ? null : displayName.toString();
    }

    private static int[] readOrigin(Object placement) {
        int[] def = {0, 0, 0};
        try {
            Object origin = placement.getClass().getMethod("getOrigin").invoke(placement);
            if (origin instanceof BlockPos bp) {
                return new int[]{bp.getX(), bp.getY(), bp.getZ()};
            }
        } catch (Exception ignore) {
        }
        return def;
    }

    private static String invokeStr(Object target, String[] names) {
        Class<?> c = target.getClass();
        for (String n : names) {
            try {
                Method m = c.getMethod(n);
                Object v = m.invoke(target);
                if (v != null) {
                    return v.toString();
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static int invokeInt(Object target, String[] names, int def) {
        Class<?> c = target.getClass();
        for (String n : names) {
            try {
                Method m = c.getMethod(n);
                Object v = m.invoke(target);
                if (v instanceof Number num) {
                    return num.intValue();
                }
            } catch (Exception ignore) {
            }
        }
        return def;
    }
}
