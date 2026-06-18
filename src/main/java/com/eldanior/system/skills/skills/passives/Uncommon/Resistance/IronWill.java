package com.eldanior.system.skills.skills.passives.Uncommon.Resistance;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class IronWill implements IPassiveCombatSkill {

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;

        // Les coups >12 dégâts réduits de 15%
        if (damage.getAmount() > 12.0f) {
            damage.setAmount(damage.getAmount() * 0.85f);
        }
        return false;
    }
}
