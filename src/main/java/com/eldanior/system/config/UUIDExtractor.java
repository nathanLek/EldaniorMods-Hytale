package com.eldanior.system.config;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.lang.reflect.Field;
import java.util.UUID;

public final class UUIDExtractor {

    private static final Field UUID_FIELD;

    static {
        try {
            UUID_FIELD = PlayerRef.class.getDeclaredField("uuid");
            UUID_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("[Eldanior] Champ UUID introuvable dans PlayerRef!", e);
        }
    }

    public static UUID getUUID(PlayerRef ref) {
        try {
            return (UUID) UUID_FIELD.get(ref);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("[Eldanior] Impossible d'extraire UUID", e);
        }
    }
}
