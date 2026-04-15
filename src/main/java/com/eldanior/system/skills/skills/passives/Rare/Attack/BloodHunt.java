package com.eldanior.system.skills.skills.passives.Rare.Attack;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import java.util.UUID;

public class BloodHunt implements IPassiveCombatSkill {
    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        UUIDComponent victimUUIDComp = store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (victimUUIDComp == null) return;
        UUID currentVictimUUID = victimUUIDComp.getUuid();
        if (currentVictimUUID.equals(attackerData.getLastVictimUUID())) {
            int stacks = attackerData.getHauntingThrustStacks();
            if (stacks < 5) { stacks++; attackerData.setHauntingThrustStacks(stacks); }
            damage.setAmount(damage.getAmount() * (1.0f + (stacks * 0.07f)));
        } else {
            attackerData.setLastVictimUUID(currentVictimUUID);
            attackerData.setHauntingThrustStacks(1);
        }
    }
}