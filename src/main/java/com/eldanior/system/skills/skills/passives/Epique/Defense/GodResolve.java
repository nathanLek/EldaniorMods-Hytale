package com.eldanior.system.skills.skills.passives.Epique.Defense;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class GodResolve implements IPassiveCombatSkill {
    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;
        float newDamage = damage.getAmount() - 12.0f;
        if (newDamage < 1.0f) newDamage = 1.0f;
        damage.setAmount(newDamage);
        return false;
    }
}