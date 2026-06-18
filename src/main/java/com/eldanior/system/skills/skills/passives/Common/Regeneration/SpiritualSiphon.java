package com.eldanior.system.skills.skills.passives.Common.Regeneration;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SpiritualSiphon implements IPassiveCombatSkill {

    private static final float CHANCE = 0.20f;
    private static final float RESTORE = 5.0f;
    private static final float RESTORE_MASTERED = 5.5f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled() || attackerRef == null) return proc;

        if (Math.random() <= CHANCE) {
            EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return proc;

            var manaStat = statMap.get(DefaultEntityStatTypes.getMana());
            if (manaStat != null) {
                float restore = mastered ? RESTORE_MASTERED : RESTORE;
                statMap.setStatValue(DefaultEntityStatTypes.getMana(), Math.min(manaStat.getMax(), manaStat.get() + restore));
                proc = true;
            }
        }
        return proc;
    }
}
