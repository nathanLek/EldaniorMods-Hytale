package com.eldanior.system.skills.skills.passives.Rare.Defense;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TitanResolve implements IPassiveCombatSkill {
    private static final float FLAT_REDUCTION = 8.0f;
    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;
        float newDamage = damage.getAmount() - FLAT_REDUCTION;
        if (newDamage < 1.0f) newDamage = 1.0f;
        damage.setAmount(newDamage);
        return false;
    }
}