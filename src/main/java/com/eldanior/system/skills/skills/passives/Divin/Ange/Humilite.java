package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Humilite (Michael, Eglise) : +15 % de degats et -10 % de degats recus par piece d'armure en moins. */
public class Humilite implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        int manque = 4 - PouvoirsDivins.armorPieces(attackerRef, store);
        if (manque <= 0) return false;
        damage.setAmount(damage.getAmount() * (1f + 0.15f * manque));
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        int manque = 4 - PouvoirsDivins.armorPieces(victimRef, store);
        if (manque <= 0) return false;
        damage.setAmount(damage.getAmount() * (1f - 0.10f * manque));
        return true;
    }
}
