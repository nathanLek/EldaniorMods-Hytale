package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class IronResolve implements IPassiveCombatSkill {

    private static final float FLAT_REDUCTION = 3.0f;
    private static final float FLAT_REDUCTION_MASTERED = 3.3f;

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled()) return proc;

        proc = true;
        float reduction = mastered ? FLAT_REDUCTION_MASTERED : FLAT_REDUCTION;
        float newDamage = Math.max(1.0f, damage.getAmount() - reduction);
        damage.setAmount(newDamage);
        return proc;
    }
}
