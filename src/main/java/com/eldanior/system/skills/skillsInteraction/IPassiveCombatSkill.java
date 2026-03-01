package com.eldanior.system.skills.skillsInteraction;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface IPassiveCombatSkill {

    // --- COMBAT ---
    // Ajout de attackerRef ici pour pouvoir identifier l'attaquant (celui qui a le passif)
    default void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {}

    // Méthode pour la défense (Déjà correcte)
    default void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {}

    // --- STATISTIQUES ---
    default float getFlatStatBonus(StatConfig stat) { return 0.0f; }
    default float getStatMultiplier(StatConfig stat) { return 1.0f; }
}