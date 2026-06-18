package com.eldanior.system.skills.skills.passives.Legendaire.Magique;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ArcaneGenesis implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null) return false;
        if (Math.random() <= 0.45f) {
            damage.setAmount(damage.getAmount() + 40.0f);
        }
        return false;
    }
}