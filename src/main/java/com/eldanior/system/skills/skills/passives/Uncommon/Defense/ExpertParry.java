package com.eldanior.system.skills.skills.passives.Uncommon.Defense;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ExpertParry implements IPassiveCombatSkill {

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;

        // 20% de chance de bloquer 20% des dégâts
        if (Math.random() <= 0.30f) {
            damage.setAmount(damage.getAmount() * 0.80f);
        }
        return false;
    }
}