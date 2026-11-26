package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Chastete (Azrael, Eglise) : contre les monstres, +150 % de degats et -25 % de degats recus. */
public class Chastete implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled() || victimRef == null || PouvoirsDivins.isPlayer(victimRef, store)) return false;
        damage.setAmount(damage.getAmount() * 2.5f);
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled() || attackerRef == null || !attackerRef.isValid() || PouvoirsDivins.isPlayer(attackerRef, store)) return false;
        damage.setAmount(damage.getAmount() * 0.75f);
        return true;
    }
}
