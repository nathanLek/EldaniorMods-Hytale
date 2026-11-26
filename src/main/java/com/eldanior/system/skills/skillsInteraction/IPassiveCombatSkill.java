package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

/**
 * Effet d'un passif. Depuis la refonte du 2026-11-24, chaque appel recoit le RANG du passif (1 Commun .. 6 Legendaire,
 * 7 pour les Anges et Peches) et s'il est maitrise (effet +10 %).
 *
 * onAttack / onDefend renvoient true quand le passif s'est declenche : cela fait progresser sa maitrise.
 * Les donnees du joueur (data) sont null pour un aventurier PNJ : l'effet doit alors s'en passer.
 */
public interface IPassiveCombatSkill {

    record RadarTarget(String name, int distance, boolean isPlayer) implements Comparable<RadarTarget> {
        @Override
        public int compareTo(RadarTarget o) { return Integer.compare(this.distance, o.distance); }
    }

    default boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                             Ref<EntityStore> victimRef, int rank, boolean mastered) { return false; }

    default boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                             Ref<EntityStore> victimRef, int rank, boolean mastered) { return false; }

    /** Bonus ajoute a la valeur finale d'une stat. */
    default float getFlatStatBonus(StatConfig stat, int rank, boolean mastered) { return 0f; }

    /** Multiplicateur de la valeur finale d'une stat. */
    default float getStatMultiplier(StatConfig stat, int rank, boolean mastered) { return 1f; }

    /** Multiplicateur de regeneration naturelle (vie, mana). */
    default float getRegenMultiplier(StatConfig stat, int rank, boolean mastered) { return 1f; }

    /** Message du radar (Instinct de Survie) ; null = ce passif n'est pas un radar. */
    default String getRadarMessage(List<RadarTarget> closestTargets, int extraMobs, int extraPlayers) { return null; }

    default NotificationStyle getRadarStyle() { return null; }
}
