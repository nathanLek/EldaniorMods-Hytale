package com.eldanior.system.skills.skills.passives.Unique.Regeneration;

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

public class EternalBlood implements IPassiveCombatSkill {
    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return false;
        if (Math.random() <= 0.50f) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return false;
            EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
            if (healthStat != null) {
                statMap.setStatValue(StatConfig.VITALITY.getStatId(), Math.min(healthStat.getMax(), healthStat.get() + healthStat.getMax() * 0.20f));
            }
        }
        return false;
    }
}