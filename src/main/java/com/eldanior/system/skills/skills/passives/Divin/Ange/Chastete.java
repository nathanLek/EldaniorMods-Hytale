package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Chasteté (Azraël) — +150% dégâts contre les mobs (combat only).
 * Aucun bonus permanent — uniquement en combat vs entités non-joueurs.
 * Église RELIGIEUX+ only.
 */
public class Chastete implements IPassiveCombatSkill {

    private static final float MOB_DAMAGE_MULTIPLIER = 2.5f; // +150% = x2.5

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer != null) return false;

        damage.setAmount(damage.getAmount() * MOB_DAMAGE_MULTIPLIER);
        return true;
    }
}
