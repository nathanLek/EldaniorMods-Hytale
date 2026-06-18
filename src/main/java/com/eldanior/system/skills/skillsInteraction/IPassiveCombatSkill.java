package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public interface IPassiveCombatSkill {

    record RadarTarget(String name, int distance, boolean isPlayer) implements Comparable<RadarTarget> {
    @Override
        public int compareTo(RadarTarget o) {
            return Integer.compare(this.distance, o.distance); // Trie par distance
        }
    }
    default boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) { return false; }
    default boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) { return false; }

    // Versions avec mastery (appelées par CombatStatsSystem) — par défaut délègue aux méthodes sans mastery
    default boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        return onAttack(damage, attackerData, store, attackerRef, victimRef);
    }
    default boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        return onDefend(damage, victimData, store, attackerRef, victimRef);
    }

    // Coûts alternatifs (% de la stat actuelle, affichés dans l'interface)
    default float getEnduranceCostPercent() { return 0f; }
    default float getLifeCostPercent() { return 0f; }

    // --- STATISTIQUES ---
    default float getFlatStatBonus(StatConfig stat) { return 0.0f; }
    default float getStatMultiplier(StatConfig stat) { return 1.0f; }

    // --- DETECTION ---
    default String getRadarMessage(List<RadarTarget> closestTargets, int extraMobs, int extraPlayers) {
        int total = closestTargets.size() + extraMobs + extraPlayers;
        if (total == 0) return null;
        return "<color:red>" + total + " présence(s) à proximité</color>";
    }
    default NotificationStyle getRadarStyle() { return null; }

    // --- REGENERATION ---
    default float getRegenMultiplier(StatConfig stat) {
        return 1.0f;
    }
}