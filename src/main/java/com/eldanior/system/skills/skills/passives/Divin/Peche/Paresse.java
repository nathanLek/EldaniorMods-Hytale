package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Paresse — Aura de ralentissement dans un rayon de 15 blocs.
 * PK only, PVP/Duel only. Aucun bonus permanent.
 * L'onDefend applique un slow à l'attaquant.
 */
public class Paresse implements IPassiveCombatSkill {

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null || !attackerRef.isValid()) return false;

        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        try {
            com.eldanior.system.config.Effects.EffectsManager.applyEffect(attackerRef, "Slow_Aura", store);
        } catch (Exception ignored) {}

        return true;
    }
}
