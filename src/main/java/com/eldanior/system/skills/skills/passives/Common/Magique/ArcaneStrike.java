package com.eldanior.system.skills.skills.passives.Common.Magique;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ArcaneStrike implements IPassiveCombatSkill {

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null) return;

        // 20% de chance d'ajouter 8 points de dégâts arcaniques
        if (Math.random() <= 0.20f) {
            damage.setAmount(damage.getAmount() + 8.0f);
        }
    }
}