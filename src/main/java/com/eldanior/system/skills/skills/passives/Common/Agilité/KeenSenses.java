package com.eldanior.system.skills.skills.passives.Common.Agilité;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class KeenSenses implements IPassiveCombatSkill {

    private static final float CRIT_BONUS = 1.02f;
    private static final float CRIT_BONUS_MASTERED = 1.022f;
    private static final float ENDURANCE_COST = 0.10f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public float getEnduranceCostPercent() { return ENDURANCE_COST; }

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) {
            return CRIT_BONUS;
        }
        return 1.0f;
    }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (!attackerData.wasLastAttackCrit()) return;

        EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap != null) {
            var staminaStat = statMap.get(StatConfig.ENDURANCE.getStatId());
            if (staminaStat != null) {
                float currentStamina = staminaStat.get();
                float cost = currentStamina * ENDURANCE_COST;
                if (currentStamina > cost) {
                    statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), currentStamina - cost);
                    lastProc = true;
                }
            }
        }
    }
}
