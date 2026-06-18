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

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled()) return proc;

        proc = true;
        float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
        damage.setAmount(damage.getAmount() * mult);
        return proc;
    }
}
