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
 * Envie — Plus le joueur perd de HP, plus sa puissance augmente.
 * 10% HP perdu = +20% dégâts. À 50% HP = +100%.
 * PK only, PVP/Duel only. Aucun bonus permanent.
 */
public class Envie implements IPassiveCombatSkill {

    private static final float BONUS_PER_PERCENT_LOST = 0.02f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;

        EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
        if (healthStat == null || healthStat.getMax() <= 0) return false;

        float hpLostPercent = 1.0f - (healthStat.get() / healthStat.getMax());
        if (hpLostPercent <= 0) return false;

        float damageMultiplier = 1.0f + (hpLostPercent * 100f * BONUS_PER_PERCENT_LOST);
        damage.setAmount(damage.getAmount() * damageMultiplier);
        return true;
    }
}
