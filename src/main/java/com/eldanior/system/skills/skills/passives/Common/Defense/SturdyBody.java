package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SturdyBody implements IPassiveCombatSkill {

    private static final float DEFENSE_BONUS = 15.0f;
    private static final float DEFENSE_BONUS_MASTERED = 16.5f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE_DEFENSE) {
            return DEFENSE_BONUS;
        }
        return 0.0f;
    }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = true;
    }
}
