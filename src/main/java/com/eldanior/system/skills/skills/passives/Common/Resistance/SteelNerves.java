package com.eldanior.system.skills.skills.passives.Common.Resistance;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SteelNerves implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;

        float currentDamage = damage.getAmount();

        // Les gros coups (>15 dégâts) sont réduits de 10%
        if (currentDamage > 15.0f) {
            damage.setAmount(currentDamage * 0.90f);
        }
    }
}