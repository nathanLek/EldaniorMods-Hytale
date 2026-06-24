package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Patience (Raphaël) — +20% HP et force aux alliés du même groupe dans un rayon de 15 blocs.
 * L'effet d'aura groupe est géré par DivineAuraSystem (système tick).
 * Cette classe donne aussi un bonus personnel de +20% HP et force.
 * Église RELIGIEUX+ only.
 */
public class Patience implements IPassiveCombatSkill {

    private static final float BONUS_MULTIPLIER = 1.20f; // +20%

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // +20% damage (force bonus)
        damage.setAmount(damage.getAmount() * BONUS_MULTIPLIER);
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;
        // +20% HP effectively = -16.67% incoming damage reduction
        damage.setAmount(damage.getAmount() * (1.0f / BONUS_MULTIPLIER));
        return true;
    }
}
