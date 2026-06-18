package com.eldanior.system.skills.skills.passives.Unique.Endurance;

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

public class RageFrenzy implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null) return false;
        if (Math.random() <= 0.60f) {
            EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return false;
            EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
            if (enduranceStat != null) {
                statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), Math.min(enduranceStat.getMax(), enduranceStat.get() + 20.0f));
            }
        }
        return false;
    }
}