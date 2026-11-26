package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.config.Effects.EffectsManager;

/** Paresse (Peche, PK) : -15 % de degats recus des joueurs, qui sont ralentis 3 s quand ils frappent le porteur. */
public class Paresse implements IPassiveCombatSkill {
    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled() || !PouvoirsDivins.isPlayer(attackerRef, store)) return false;
        damage.setAmount(damage.getAmount() * 0.85f);
        try { EffectsManager.applyEffect(attackerRef, "Eld_Paresse_Lenteur", store); } catch (Exception ignored) { }
        return true;
    }
}
