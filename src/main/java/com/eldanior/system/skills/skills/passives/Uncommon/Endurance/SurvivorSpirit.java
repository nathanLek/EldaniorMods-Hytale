package com.eldanior.system.skills.skills.passives.Uncommon.Endurance;

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

public class SurvivorSpirit implements IPassiveCombatSkill {

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return false;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;

        EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
        if (enduranceStat != null) {
            float maxEndurance = enduranceStat.getMax();
            float currentEndurance = enduranceStat.get();

            // Si endurance < 25%, 20% de chance de restaurer 40 points
            if (currentEndurance < (maxEndurance * 0.25f)) {
                if (Math.random() <= 0.30f) {
                    float newEndurance = Math.min(maxEndurance, currentEndurance + 40.0f);
                    statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), newEndurance);
                }
            }
        }
        return false;
    }
}