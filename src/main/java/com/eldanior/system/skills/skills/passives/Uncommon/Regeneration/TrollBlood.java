package com.eldanior.system.skills.skills.passives.Uncommon.Regeneration;

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

public class TrollBlood implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return;

        // 20% de chance de se soigner de 8% vie max
        if (Math.random() <= 0.20f) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;

            EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
            if (healthStat != null) {
                float healAmount = healthStat.getMax() * 0.08f;
                float newHealth = Math.min(healthStat.getMax(), healthStat.get() + healAmount);
                statMap.setStatValue(StatConfig.VITALITY.getStatId(), newHealth);
            }
        }
    }
}
