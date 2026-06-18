package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DeepSlash implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float FLAT_BONUS = 1.0f;
    private static final float FLAT_BONUS_MASTERED = 1.1f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = true;

        float bonus = mastered ? FLAT_BONUS_MASTERED : FLAT_BONUS;
        float originalDamage = damage.getAmount();
        damage.setAmount(originalDamage + bonus);

        LOGGER.atInfo().log("[Skill] DEEP_SLASH : " + originalDamage + " -> " + (originalDamage + bonus) + " (+" + bonus + ")");
    }
}
