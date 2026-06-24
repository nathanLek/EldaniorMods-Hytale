package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Charité (Gabriel) — +150% HP. Quand un allié du groupe perd de la vie,
 * le joueur donne sa propre vie pour le soigner. Désactivé si le joueur < 10% HP.
 * L'effet de soins d'alliés est géré par DivineAuraSystem.
 * Cette classe gère le bonus de HP (+150%) via réduction de dégâts.
 * Église RELIGIEUX+ only.
 */
public class Charite implements IPassiveCombatSkill {

    private static final float HP_MULTIPLIER = 2.5f; // +150% HP = x2.5

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;

        // +150% HP effectively means incoming damage is reduced proportionally
        damage.setAmount(damage.getAmount() / HP_MULTIPLIER);
        return true;
    }
}
