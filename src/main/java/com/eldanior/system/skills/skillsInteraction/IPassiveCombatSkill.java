package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface IPassiveCombatSkill {

    // --- COMBAT ---
    // Méthode pour l'attaque
    default void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> victimRef) {}

    // Méthode pour la défense (avec notre paramètre victimRef, et surtout "default" et "{}")
    default void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {}

    // --- STATISTIQUES ---
    // Pour ajouter des points fixes
    default float getFlatStatBonus(StatConfig stat) { return 0.0f; }

    // Pour multiplier une stat (ex: 1.2f = +20% Vitesse, +20% Chance de Crit...)
    default float getStatMultiplier(StatConfig stat) { return 1.0f; }
}