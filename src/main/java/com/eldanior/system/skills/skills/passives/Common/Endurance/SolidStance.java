package com.eldanior.system.skills.skills.passives.Common.Endurance;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SolidStance implements IPassiveCombatSkill {

    private static final float THRESHOLD = 0.80f;
    private static final float REDUCTION = 0.90f;
    private static final float REDUCTION_MASTERED = 0.89f;
    private static final float ENDURANCE_COST = 0.20f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public float getEnduranceCostPercent() { return ENDURANCE_COST; }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (damage.isCancelled() || victimRef == null) return;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
        if (enduranceStat != null && enduranceStat.get() >= (enduranceStat.getMax() * THRESHOLD)) {
            float currentStamina = enduranceStat.get();
            float cost = currentStamina * ENDURANCE_COST;
            if (currentStamina > cost) {
                statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), currentStamina - cost);
                float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
                damage.setAmount(damage.getAmount() * mult);
                lastProc = true;

                if (attackerRef != null) {
                    PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        NotificationHelper.sendNotification(playerRef,
                                "<color:green>Posture Solide : -10% de degats </color>", NotificationStyle.Success);
                    }
                }
            }
        }
    }
}

