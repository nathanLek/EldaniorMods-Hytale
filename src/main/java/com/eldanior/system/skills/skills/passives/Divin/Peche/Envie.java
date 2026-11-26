package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Envie (Peche, PK) : +1 % de degats par % de vie perdue (max +80 %) ; +25 % contre un joueur de plus haut niveau. */
public class Envie implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        float ratio = PouvoirsDivins.healthRatio(attackerRef, store);
        float mult = 1f + (ratio < 0 ? 0f : Math.min(0.80f, 1f - ratio));
        PlayerLevelData victim = PouvoirsDivins.data(victimRef, store);
        if (data != null && victim != null && victim.getLevel() > data.getLevel()) mult += 0.25f;
        if (mult <= 1f) return false;
        damage.setAmount(damage.getAmount() * mult);
        return true;
    }
}
