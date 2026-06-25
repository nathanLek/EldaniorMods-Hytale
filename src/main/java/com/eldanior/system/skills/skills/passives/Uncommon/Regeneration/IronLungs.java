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

public class IronLungs implements IPassiveCombatSkill {

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null) return false;

        // 30% de chance de restaurer 12 endurance par attaque
        if (Math.random() <= 0.45f) {
            EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return false;

            EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
            if (enduranceStat != null) {
                float newEndurance = Math.min(enduranceStat.getMax(), enduranceStat.get() + 12.0f);
                statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), newEndurance);
            }
        }
        return false;
    }
}
