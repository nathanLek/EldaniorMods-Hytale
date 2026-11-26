package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Patience (Raphael, Eglise) : +5 % de degats et -5 % de degats recus par membre du groupe a moins de 15 blocs (4 au plus). */
public class Patience implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        int n = Math.min(4, PouvoirsDivins.alliesNear(attackerRef, store, 15).size());
        if (n == 0) return false;
        damage.setAmount(damage.getAmount() * (1f + 0.05f * n));
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        int n = Math.min(4, PouvoirsDivins.alliesNear(victimRef, store, 15).size());
        if (n == 0) return false;
        damage.setAmount(damage.getAmount() * (1f - 0.05f * n));
        return true;
    }
}
