package com.survivalaid;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectionSyncStore {
    private static final ConcurrentHashMap<UUID, ProjectionClientState> PROJECTIONS = new ConcurrentHashMap<>();

    private ProjectionSyncStore() {
    }

    public static ProjectionClientState get(UUID uuid) {
        return PROJECTIONS.get(uuid);
    }

    public static void set(UUID uuid, ProjectionClientState state) {
        if (state == null || state.name == null || state.name.isEmpty()) {
            PROJECTIONS.remove(uuid);
        } else {
            PROJECTIONS.put(uuid, state);
        }
    }

    public static void remove(UUID uuid) {
        PROJECTIONS.remove(uuid);
    }
}
