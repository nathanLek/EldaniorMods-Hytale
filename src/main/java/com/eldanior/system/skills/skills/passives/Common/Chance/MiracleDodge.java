package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MiracleDodge implements IPassiveCombatSkill {

    private static final float CHANCE = 0.03f;
    private static final float CHANCE_MASTERED = 0.033f;
    private static final float ENDURANCE_COST = 0.15f; // 15%

    @Override
    public float getEnduranceCostPercent() { return ENDURANCE_COST; }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled()) return proc;

        float chance = mastered ? CHANCE_MASTERED : CHANCE;
        if (Math.random() <= chance) {
            // Vérifier et consommer 15% d'endurance
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                float currentStamina = statMap.get(DefaultEntityStatTypes.getStamina()).get();
                float cost = currentStamina * ENDURANCE_COST;
                if (currentStamina > cost) {
                    statMap.setStatValue(DefaultEntityStatTypes.getStamina(), currentStamina - cost);
                    damage.setCancelled(true);
                    proc = true;

                    if (victimRef != null) {
                        PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
                        if (playerRef != null) {
                            NotificationHelper.sendNotification(playerRef, "<color:yellow>Esquive Miraculeuse !</color>", NotificationStyle.Success);
                        }
                    }
                }
            }
        }
        return proc;
    }
}