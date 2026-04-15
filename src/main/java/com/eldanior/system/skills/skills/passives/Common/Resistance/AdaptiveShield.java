package com.eldanior.system.skills.skills.passives.Common.Resistance;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AdaptiveShield implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;

        // 10% de chance de réduire un coup de 20%
        if (Math.random() <= 0.10f) {
            float currentDamage = damage.getAmount();
            damage.setAmount(currentDamage * 0.80f);
        }
    }
}