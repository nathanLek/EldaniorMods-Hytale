package com.eldanior.system.skills.skills.passives.Rare.Regeneration;

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

public class HydraBlood implements IPassiveCombatSkill {
    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || victimRef == null) return;
        if (Math.random() <= 0.25f) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;
            EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
            if (healthStat != null) {
                float healAmount = healthStat.getMax() * 0.12f;
                statMap.setStatValue(StatConfig.VITALITY.getStatId(), Math.min(healthStat.getMax(), healthStat.get() + healAmount));
            }
        }
    }
}