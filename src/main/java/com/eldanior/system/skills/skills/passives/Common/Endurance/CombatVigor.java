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

public class CombatVigor implements IPassiveCombatSkill {

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null) return;

        // 20% de chance de restaurer de l'endurance en frappant
        if (Math.random() <= 0.20f) {
            EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;

            EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
            if (enduranceStat != null) {
                float maxEndurance = enduranceStat.getMax();
                float currentEndurance = enduranceStat.get();

                // Restaure 5 points sans dépasser le maximum
                float newEndurance = Math.min(maxEndurance, currentEndurance + 5.0f);
                statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), newEndurance);
            }
        }
    }
}