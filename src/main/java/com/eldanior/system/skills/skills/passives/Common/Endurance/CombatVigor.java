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

public class CombatVigor implements IPassiveCombatSkill {

    private static final float CHANCE = 0.20f;
    private static final float RESTORE = 5.0f;
    private static final float RESTORE_MASTERED = 5.5f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (damage.isCancelled() || attackerRef == null) return;

        if (Math.random() <= CHANCE) {
            EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;

            EntityStatValue enduranceStat = statMap.get(StatConfig.ENDURANCE.getStatId());
            if (enduranceStat != null) {
                float restore = mastered ? RESTORE_MASTERED : RESTORE;
                float newEndurance = Math.min(enduranceStat.getMax(), enduranceStat.get() + restore);
                statMap.setStatValue(StatConfig.ENDURANCE.getStatId(), newEndurance);
                lastProc = true;

                if (attackerRef != null) {
                    PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                    if (playerRef != null) {
                        NotificationHelper.sendNotification(playerRef,
                                "<color:green>Vigueur Combative: +5 d'Endurance </color>", NotificationStyle.Success);
                    }
                }
            }
        }
    }
}
