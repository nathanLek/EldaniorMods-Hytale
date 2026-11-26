package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Orgueil (Peche, PK) : +5 % de degats et -3 % de degats recus par piece d'armure portee. */
public class Orgueil implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        int pieces = PouvoirsDivins.armorPieces(attackerRef, store);
        if (pieces <= 0) return false;
        damage.setAmount(damage.getAmount() * (1f + 0.05f * pieces));
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        int pieces = PouvoirsDivins.armorPieces(victimRef, store);
        if (pieces <= 0) return false;
        damage.setAmount(damage.getAmount() * (1f - 0.03f * pieces));
        return true;
    }
}
