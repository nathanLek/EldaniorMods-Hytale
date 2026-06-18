package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HunterGuard implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float REDUCTION = 0.90f;
    private static final float REDUCTION_MASTERED = 0.89f;
    private static final float ENDURANCE_COST = 0.15f;

    @Override
    public float getEnduranceCostPercent() { return ENDURANCE_COST; }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled() || attackerRef == null || !attackerRef.isValid()) return proc;

        boolean isMob = store.getComponent(attackerRef, EldaniorSystem.get().getMobLevelDataType()) != null;
        if (!isMob) return proc;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap != null) {
            var staminaStat = statMap.get(StatConfig.ENDURANCE.getStatId());
            if (staminaStat != null) {
                float currentStamina = staminaStat.get();
                float cost = currentStamina * ENDURANCE_COST;
                LOGGER.atInfo().log("[HunterGuard] Stamina: " + currentStamina + " | Cost: " + cost);
                if (currentStamina > cost) {
                    statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), currentStamina - cost);
                    float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
                    damage.setAmount(damage.getAmount() * mult);
                    proc = true;
                }
            }
        }
        return proc;
    }
}
