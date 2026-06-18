package com.eldanior.system.skills.skills.passives.Common.Resistance;

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

public class Tenacity implements IPassiveCombatSkill {

    private static final float HP_THRESHOLD = 0.30f;
    private static final float HP_THRESHOLD_MASTERED = 0.35f;
    private static final float REDUCTION = 0.92f;
    private static final float REDUCTION_MASTERED = 0.91f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (damage.isCancelled() || victimRef == null) return;

        EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
        if (healthStat == null) return;

        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();
        float threshold = mastered ? HP_THRESHOLD_MASTERED : HP_THRESHOLD;

        if (currentHealth < (maxHealth * threshold)) {
            float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
            damage.setAmount(damage.getAmount() * mult);
            lastProc = true;

            PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef, "<color:red>Ténacité : -" + (int)((1f - mult) * 100) + "% de dégâts (vie basse)</color>", NotificationStyle.Success);
            }
        }
    }
}
