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

public class SecondWind implements IPassiveCombatSkill {

    private static final float CHANCE = 0.15f;
    private static final float THRESHOLD = 0.20f;
    private static final float RESTORE = 25.0f;
    private static final float RESTORE_MASTERED = 27.5f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (damage.isCancelled() || victimRef == null) return;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
        if (enduranceStat != null) {
            float currentEndurance = enduranceStat.get();
            if (currentEndurance < (enduranceStat.getMax() * THRESHOLD)) {
                if (Math.random() <= CHANCE) {
                    float restore = mastered ? RESTORE_MASTERED : RESTORE;
                    statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), Math.min(enduranceStat.getMax(), currentEndurance + restore));
                    lastProc = true;

                    if (attackerRef != null) {
                        PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                        if (playerRef != null) {
                            NotificationHelper.sendNotification(playerRef,
                                    "<color:green>Second Souffle: +25 d'Endurance </color>", NotificationStyle.Success);
                        }
                    }
                }
            }
        }
    }
}
