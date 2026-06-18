package com.eldanior.system.skills.skills.passives.Common.Vie;

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

public class Perseverance implements IPassiveCombatSkill {

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        lastProc = false;
        if (damage.isCancelled() || victimRef == null) return;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
        if (healthStat == null) return;

        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();

        // Sous 25% de vie, chaque coup reçu restaure 2% de vie max
        if (currentHealth < (maxHealth * 0.25f)) {
            float healAmount = maxHealth * 0.02f;
            float newHealth = Math.min(maxHealth, currentHealth + healAmount);
            statMap.setStatValue(StatConfig.VITALITY.getStatId(), newHealth);
            lastProc = true;

            PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef, "<color:green>Persévérance : +" + String.format("%.0f", healAmount) + " PV</color>", NotificationStyle.Success);
            }
        }
    }
}