package com.eldanior.system.skills.skills.passives.Rare.Attack;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CrimsonBlade implements IPassiveCombatSkill {
    private static final float FLAT_BONUS = 4.0f;

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        damage.setAmount(damage.getAmount() + FLAT_BONUS);
    }
}
