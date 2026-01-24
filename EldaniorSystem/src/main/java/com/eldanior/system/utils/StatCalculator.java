package com.eldanior.system.utils;

import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StatCalculator {

    @SuppressWarnings("unchecked")
    public static void updatePlayerStats(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {

        // --- 1. Récupération de la Carte des Stats (EntityStatMap) ---
        // On récupère le composant via le module
        EntityStatMap statMap = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap != null) {

            // === VITALITÉ (PV MAX) ===
            int healthIndex = DefaultEntityStatTypes.getHealth();
            // Formule : +5 PV par point de Vitalité
            float healthBonus = data.getVitality() * 5.0f;

            // Nettoyage ancien bonus
            statMap.removeModifier(healthIndex, "Eldanior_Vitality_Bonus");

            // Application nouveau bonus
            if (healthBonus > 0) {
                StaticModifier healthMod = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, healthBonus);
                statMap.putModifier(healthIndex, "Eldanior_Vitality_Bonus", healthMod);
            }

            // === INTELLIGENCE (MANA MAX) ===
            int manaIndex = DefaultEntityStatTypes.getMana();
            // Formule : +10 Mana par point d'Intelligence
            float manaBonus = data.getIntelligence() * 10.0f;

            statMap.removeModifier(manaIndex, "Eldanior_Intelligence_Bonus");

            if (manaBonus > 0) {
                StaticModifier manaMod = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, manaBonus);
                statMap.putModifier(manaIndex, "Eldanior_Intelligence_Bonus", manaMod);
            }
        }

        // === AGILITÉ (VITESSE) ===
        /* TODO: La gestion de la vitesse dépend de ta version d'Hytale.
           Comme 'PlayerCapabilities' est introuvable, on laisse ça de côté pour l'instant
           pour que la Vie et le Mana fonctionnent.

           Si tu veux activer la vitesse plus tard, il faudra trouver où se cache 'setWalkSpeed'
           (souvent dans PhysicsComponent ou Player).
        */
        // float speedBonus = data.getAgility() * 0.002f;
    }
}