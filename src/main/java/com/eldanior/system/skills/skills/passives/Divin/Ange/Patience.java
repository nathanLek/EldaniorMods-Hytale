package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Patience (Raphaël) — +20% HP et force aux alliés du groupe dans un rayon de 15 blocs.
 * Le bonus s'applique aussi au porteur : +20% HP (permanent) + 20% dégâts (combat).
 * L'effet d'aura sur les alliés est géré par DivineAuraSystem.
 * Église RELIGIEUX+ only.
 */
public class Patience implements IPassiveCombatSkill {

    // +20% HP permanent (proportionnel aux points du joueur)
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 1.20f;
        return 1.0f;
    }

    // +20% force en combat
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        damage.setAmount(damage.getAmount() * 1.20f);
        return true;
    }
}
