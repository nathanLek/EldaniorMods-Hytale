package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Paresse (Sloth) — Aura de ralentissement (slow) dans un rayon de 15 blocs.
 * L'effet d'aura est géré par DivineAuraSystem (système tick).
 * Cette classe sert de marqueur dans le PassiveSkill enum.
 * L'onDefend applique un léger slow au contact direct.
 * PK only, PVP/Duel only.
 */
public class Paresse implements IPassiveCombatSkill {

    private static final float SLOW_MULTIPLIER = 0.70f; // -30% speed on attacker

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null || !attackerRef.isValid()) return false;

        // PVP only - check attacker is a player
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        // Apply slow effect to the attacker via effect system
        try {
            com.eldanior.system.config.Effects.EffectsManager.applyEffect(attackerRef, "Slow_Aura", store);
        } catch (Exception ignored) {}

        return true;
    }

    @Override
    public float getStatMultiplier(StatConfig stat) {
        // This skill doesn't give stats to the owner, the aura affects others
        return 1.0f;
    }
}
