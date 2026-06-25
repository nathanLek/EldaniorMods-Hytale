package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MinorParry implements IPassiveCombatSkill {

    private static final float CHANCE = 0.20f;
    private static final float REDUCTION = 0.85f;
    private static final float REDUCTION_MASTERED = 0.835f;
    private static final float ENDURANCE_COST = 0.20f;

    @Override
    public float getEnduranceCostPercent() { return ENDURANCE_COST; }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled()) return proc;

        if (Math.random() <= CHANCE) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                var staminaStat = statMap.get(StatConfig.ENDURANCE.getStatId());
                if (staminaStat != null) {
                    float currentStamina = staminaStat.get();
                    float cost = currentStamina * ENDURANCE_COST;
                    if (currentStamina > cost) {
                        statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), currentStamina - cost);
                        float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
                        damage.setAmount(damage.getAmount() * mult);
                        proc = true;

                        PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
                        if (playerRef != null) {
                            NotificationHelper.sendNotification(playerRef, "<color:yellow>Parade réussie ! (-15%)</color>", NotificationStyle.Success);
                        }
                    }
                }
            }
        }
        return proc;
    }
}
