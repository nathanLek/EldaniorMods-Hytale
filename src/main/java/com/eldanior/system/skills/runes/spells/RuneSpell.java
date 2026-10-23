package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Sort de rune branche sur le moteur commun ({@link RuneSpellRuntime}).
 *
 * Cycle : l'interaction JSON "RuneSpell" (champ "Spell" = {@link #id()}) a deja verifie le mana, joue l'animation,
 * consomme le mana et lance la recharge ; elle met le lancement en file. Au tick suivant du lanceur, le moteur appelle
 * {@link #start} avec un CommandBuffer valide. Un sort instantane fait tout dans start et renvoie null ; un sort qui
 * dure renvoie une {@link Instance} que le moteur fait avancer a chaque tick jusqu'a ce qu'elle se termine.
 */
public interface RuneSpell {

    /** Identifiant stable, identique au champ "Spell" de l'interaction JSON (ex. "Bourrasque"). */
    String id();

    /** Execute le sort. Renvoie null s'il est instantane, sinon l'instance qui continue sur les ticks suivants. */
    Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb);

    /** Etat d'un sort qui dure (zone, invocation, canalisation...). */
    interface Instance {
        /** Avance d'un tick. Renvoie true quand le sort est termine (le moteur appelle alors {@link #cleanup}). */
        boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb);

        /** Fin normale, interruption ou erreur : retire les entites et effets temporaires. */
        default void cleanup(CommandBuffer<EntityStore> cb) {}

        /** Deconnexion du lanceur (pas de CommandBuffer disponible) : liberer ce qui peut l'etre. */
        default void abort() {}

        /** true pour recevoir les clics du lanceur pendant le sort (clic gauche / droit, entite visee). */
        default boolean listensClicks() { return false; }

        /**
         * Le lanceur vient d'infliger des degats (montant brut, avant modificateurs). Appele pendant le traitement des degats :
         * ne rien appliquer ici, seulement memoriser (appliquer dans tick).
         */
        default void onOwnerHit(Ref<EntityStore> target, float amount) {}

        /** Clic du lanceur (livre sur le thread du monde, avant tick). target = entite sous le curseur, ou null. */
        default void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {}
    }
}
