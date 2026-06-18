package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class LuckyStrike implements IPassiveCombatSkill {

    private static final float CRIT_BONUS = 3.0f;
    private static final float CRIT_BONUS_MASTERED = 3.3f;

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) {
            return CRIT_BONUS;
        }
        return 0.0f;
    }

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = attackerData.wasLastAttackCrit();
        return proc;
    }
}
