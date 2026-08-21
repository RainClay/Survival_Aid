package com.survivalaid;

public final class ProjectionClientState {
    public final String name;
    public final String rangeType;
    public final int minY;
    public final int maxY;
    public final int originX;
    public final int originY;
    public final int originZ;

    public ProjectionClientState(String name, String rangeType, int minY, int maxY, int originX, int originY, int originZ) {
        this.name = name;
        this.rangeType = rangeType;
        this.minY = minY;
        this.maxY = maxY;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
    }

    public boolean isLayerFiltered() {
        return !"ALL".equalsIgnoreCase(rangeType);
    }

    public static ProjectionClientState parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new ProjectionClientState("", "ALL", -1, -1, 0, 0, 0);
        }
        String[] p = raw.split("\u0001", -1);
        String name = p.length > 0 ? p[0] : "";
        String rt = p.length > 1 ? p[1] : "ALL";
        int minY = p.length > 2 ? toInt(p[2], -1) : -1;
        int maxY = p.length > 3 ? toInt(p[3], -1) : -1;
        int originX = p.length > 4 ? toInt(p[4], 0) : 0;
        int originY = p.length > 5 ? toInt(p[5], 0) : 0;
        int originZ = p.length > 6 ? toInt(p[6], 0) : 0;
        return new ProjectionClientState(name, rt, minY, maxY, originX, originY, originZ);
    }

    private static int toInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
