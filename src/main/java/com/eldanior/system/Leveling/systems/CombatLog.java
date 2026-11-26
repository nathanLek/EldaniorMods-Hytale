package com.eldanior.system.Leveling.systems;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Journal de combat (/es combat log, admin, 2026-11-25) : chaque coup donne ou recu par le joueur est ecrit dans son chat,
 * avec les etapes de CombatStatsSystem (esquive et sa raison, Force, critique, defense). Les reductions appliquees apres
 * (armure vanilla, PV virtuels des monstres) n'y figurent pas.
 */
public final class CombatLog {

    private CombatLog() {}

    private static final Set<UUID> ON = ConcurrentHashMap.newKeySet();

    /** Active ou coupe le journal ; renvoie le nouvel etat. */
    public static boolean toggle(UUID uuid) {
        if (ON.remove(uuid)) return false;
        ON.add(uuid);
        return true;
    }

    public static boolean isOn(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ON.isEmpty() || ref == null || !ref.isValid()) return false;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        return u != null && ON.contains(u.getUuid());
    }

    public static void send(Ref<EntityStore> ref, Store<EntityStore> store, String text) {
        PlayerRef p = store.getComponent(ref, PlayerRef.getComponentType());
        if (p != null) p.sendMessage(Message.raw(text));
    }

    /** Nom court d'une entite pour le journal : type du PNJ ou « joueur ». */
    public static String name(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return "?";
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null) return npc.getNPCTypeId();
        PlayerRef p = store.getComponent(ref, PlayerRef.getComponentType());
        return p != null ? p.getUsername() : "?";
    }

    public static String num(float v) { return String.format("%.1f", v); }
}
