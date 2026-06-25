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
 * Colère — +200% force, agilité et endurance si HP > 80%.
 * PK only, PVP/Duel only. Aucun bonus permanent — uniquement actif en combat si HP > 80%.
 */
public class Colere implements IPassiveCombatSkill {

    private static final float DAMAGE_MULTIPLIER = 3.0f; // +200% = x3

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;

        EntityStatValue healthStat = statMap.get(StatConfig.VITALITY.getStatId());
        if (healthStat == null) return false;

        if (healthStat.get() / healthStat.getMax() > 0.80f) {
            damage.setAmount(damage.getAmount() * DAMAGE_MULTIPLIER);
            return true;
        }
        return false;
    }
}
