package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StoneSkin implements IPassiveCombatSkill {

    private static final float REDUCTION = 0.95f;
    private static final float REDUCTION_MASTERED = 0.945f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;
        if (damage.isCancelled()) return;

        lastProc = true;
        float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
        damage.setAmount(damage.getAmount() * mult);
    }
}
