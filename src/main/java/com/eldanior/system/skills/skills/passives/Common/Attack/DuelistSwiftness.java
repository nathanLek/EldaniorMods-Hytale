package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DuelistSwiftness implements IPassiveCombatSkill {

    private static final float SPEED_BONUS = 1.05f;           // +5% vitesse d'attaque
    private static final float SPEED_BONUS_MASTERED = 1.055f;  // +5.5% si maîtrisé

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) {
            return SPEED_BONUS;
        }
        return 1.0f;
    }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = true;
    }
}