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

    private static final float CHANCE = 0.15f;
    private static final float HEAL_PERCENT = 0.05f;
    private static final float HEAL_PERCENT_MASTERED = 0.055f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (damage.isCancelled() || victimRef == null) return;

        if (Math.random() <= CHANCE) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;

            EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
            if (healthStat != null) {
                float percent = mastered ? HEAL_PERCENT_MASTERED : HEAL_PERCENT;
                float healAmount = healthStat.getMax() * percent;
                statMap.setStatValue(StatConfig.VITALITY.getStatId(), Math.min(healthStat.getMax(), healthStat.get() + healAmount));
                lastProc = true;
            }
        }
    }
}