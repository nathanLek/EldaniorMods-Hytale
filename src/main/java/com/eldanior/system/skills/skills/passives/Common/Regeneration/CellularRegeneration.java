package com.eldanior.system.skills.skills.passives.Common.Regeneration;

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

public class CellularRegeneration implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return;

        // 15% de chance de se soigner en encaissant un coup
        if (Math.random() <= 0.15f) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;

            EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
            if (healthStat != null) {
                float maxHealth = healthStat.getMax();
                float currentHealth = healthStat.get();

                // Soin de 5% de la vie maximale
                float healAmount = maxHealth * 0.05f;
                float newHealth = Math.min(maxHealth, currentHealth + healAmount);

                statMap.setStatValue(StatConfig.VITALITY.getStatId(), newHealth);
            }
        }
    }
}