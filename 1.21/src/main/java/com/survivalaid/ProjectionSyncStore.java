package com.survivalaid;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectionSyncStore {
    private static final ConcurrentHashMap<UUID, String> PROJECTIONS = new ConcurrentHashMap<>();

    private ProjectionSyncStore() {
    }

    public static String get(UUID uuid) {
        return PROJECTIONS.get(uuid);
    }

    public static void set(UUID uuid, String name) {
        if (name == null || name.isEmpty()) {
            PROJECTIONS.remove(uuid);
        } else {
            PROJECTIONS.put(uuid, name);
        }
    }

    public static void remove(UUID uuid) {
        PROJECTIONS.remove(uuid);
    }
}
