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

        if (data == null) return;

        EntityStatMap statMap = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap != null) {

            // === 1. VITALITÉ (Objectif 500 PV) ===
            int healthIndex = DefaultEntityStatTypes.getHealth();
            float healthBonus = data.getVitality() * 0.16f;

            statMap.removeModifier(healthIndex, "Eldanior_Vitality");
            if (healthBonus > 0) {
                StaticModifier healthMod = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, healthBonus);
                statMap.putModifier(healthIndex, "Eldanior_Vitality", healthMod);
            }

            // === 2. INTELLIGENCE (Objectif 10 000 Mana) ===
            int manaIndex = DefaultEntityStatTypes.getMana();

            // FORMULE : 3.33 mana par point d'intelligence
            // 3000 pts * 3.33 = 9990 (+10 base = 10 000)
            float manaBonus = data.getIntelligence() * 3.33f;

            statMap.removeModifier(manaIndex, "Eldanior_Intelligence");
            if (manaBonus > 0) {
                StaticModifier manaMod = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, manaBonus);
                statMap.putModifier(manaIndex, "Eldanior_Intelligence", manaMod);
            }

            // === 3. ENDURANCE (Stamina) ===
            int staminaIndex = DefaultEntityStatTypes.getStamina();
            float staminaBonus = data.getEndurance() * 5.0f;

            statMap.removeModifier(staminaIndex, "Eldanior_Endurance");
            if (staminaBonus > 0) {
                StaticModifier staminaMod = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, staminaBonus);
                statMap.putModifier(staminaIndex, "Eldanior_Endurance", staminaMod);
            }
        }
    }
}