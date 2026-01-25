package com.eldanior.system.utils;

import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.ClassManager;
import com.eldanior.system.rpg.classes.ClassModel;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StatCalculator {

    public static void updatePlayerStats(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {
        if (data == null) return;

        EntityStatMap statMap = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        // Récupération des bonus de classe
        ClassModel model = ClassManager.get(data.getPlayerClassId());
        int bonusVit = (model != null) ? model.getBonusVit() : 0;
        int bonusInt = (model != null) ? model.getBonusInt() : 0;
        int bonusEnd = (model != null) ? model.getBonusEnd() : 0;

        // === 1. VITALITÉ (Points Joueur + Bonus Classe) ===
        int totalVit = data.getVitality() + bonusVit;
        float healthBonus = totalVit * 0.16f; // Ton ratio actuel

        statMap.removeModifier(DefaultEntityStatTypes.getHealth(), "Eldanior_Vitality");
        if (healthBonus > 0) {
            statMap.putModifier(DefaultEntityStatTypes.getHealth(), "Eldanior_Vitality",
                    new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, healthBonus));
        }

        // === 2. INTELLIGENCE (Points Joueur + Bonus Classe) ===
        int totalInt = data.getIntelligence() + bonusInt;
        float manaBonus = totalInt * 3.33f; // Ton ratio actuel

        statMap.removeModifier(DefaultEntityStatTypes.getMana(), "Eldanior_Intelligence");
        if (manaBonus > 0) {
            statMap.putModifier(DefaultEntityStatTypes.getMana(), "Eldanior_Intelligence",
                    new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, manaBonus));
        }

        // === 3. ENDURANCE (Points Joueur + Bonus Classe) ===
        int totalEnd = data.getEndurance() + bonusEnd;
        float staminaBonus = totalEnd * 5.0f; // Ton ratio actuel

        statMap.removeModifier(DefaultEntityStatTypes.getStamina(), "Eldanior_Endurance");
        if (staminaBonus > 0) {
            statMap.putModifier(DefaultEntityStatTypes.getStamina(), "Eldanior_Endurance",
                    new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, staminaBonus));
        }
    }
}