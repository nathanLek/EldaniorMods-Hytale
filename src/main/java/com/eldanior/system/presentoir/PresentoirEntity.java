package com.eldanior.system.presentoir;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Marque l'entite decor d'un presentoir : cle du presentoir et apparence montree. Jamais sauvegardee. */
public final class PresentoirEntity implements Component<EntityStore> {

    private static ComponentType<EntityStore, PresentoirEntity> type;

    private final String key, look;

    public PresentoirEntity() { this("", ""); }

    PresentoirEntity(String key, String look) {
        this.key = key;
        this.look = look;
    }

    public static void setType(ComponentType<EntityStore, PresentoirEntity> registered) { type = registered; }

    static ComponentType<EntityStore, PresentoirEntity> type() { return type; }

    String key() { return key; }

    String look() { return look; }

    @Override
    public Component<EntityStore> clone() { return new PresentoirEntity(key, look); }
}
