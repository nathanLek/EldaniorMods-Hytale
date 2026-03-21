package com.eldanior.system.skills.skills.passives.Common.Endurance;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SecondWind implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
        if (enduranceStat != null) {
            float maxEndurance = enduranceStat.getMax();
            float currentEndurance = enduranceStat.get();

            // Si l'endurance est sous les 20%
            if (currentEndurance < (maxEndurance * 0.20f)) {
                // 15% de chance de récupérer 25 points
                if (Math.random() <= 0.15f) {
                    float newEndurance = Math.min(maxEndurance, currentEndurance + 25.0f);
                    statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), newEndurance);
                }
            }
        }
    }
}