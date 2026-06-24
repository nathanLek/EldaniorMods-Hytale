package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Envie (Envy) — Plus le joueur perd de HP, plus sa puissance augmente.
 * 10% HP perdu = +20% dégâts. À 50% HP = +100%.
 * PK only, PVP/Duel only.
 */
public class Envie implements IPassiveCombatSkill {

    // 2% damage bonus per 1% HP lost
    private static final float BONUS_PER_PERCENT_LOST = 0.02f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // PVP only check
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        // Get attacker's current HP percentage
        EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;

        EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
        if (healthStat == null || healthStat.getMax() <= 0) return false;

        float hpPercent = healthStat.get() / healthStat.getMax();
        float hpLostPercent = 1.0f - hpPercent; // ex: at 50% HP → 0.5 lost

        if (hpLostPercent <= 0) return false;

        // 10% HP lost = +20% damage → ratio is 2x
        float damageMultiplier = 1.0f + (hpLostPercent * 100f * BONUS_PER_PERCENT_LOST);
        damage.setAmount(damage.getAmount() * damageMultiplier);
        return true;
    }
}
