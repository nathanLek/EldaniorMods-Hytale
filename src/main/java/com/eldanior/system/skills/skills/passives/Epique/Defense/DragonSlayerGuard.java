package com.eldanior.system.skills.skills.passives.Epique.Defense;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DragonSlayerGuard implements IPassiveCombatSkill {
    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null || !attackerRef.isValid()) return false;
        boolean isMob = store.getComponent(attackerRef, EldaniorSystem.get().getMobLevelDataType()) != null;
        if (isMob) {
            damage.setAmount(damage.getAmount() * 0.75f);
        }
        return false;
    }
}