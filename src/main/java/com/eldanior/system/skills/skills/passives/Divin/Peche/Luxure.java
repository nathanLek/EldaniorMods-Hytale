package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.awt.Color;

/**
 * Luxure (Lust) — 5% de chance de voler 10% de la fortune (or) du joueur tué.
 * Cooldown 2 jours (temps réel). PK only, PVP/Duel only.
 */
public class Luxure implements IPassiveCombatSkill {

    private static final float STEAL_CHANCE = 0.10f;
    private static final float GOLD_STEAL_PERCENT = 0.10f;
    private static final float COOLDOWN_SECONDS = 172800f; // 2 jours

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // PVP only
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        // Check if this hit will kill the target
        EntityStatMap victimStats = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStats == null) return false;
        EntityStatValue victimHealth = victimStats.get(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth());
        if (victimHealth == null || victimHealth.get() - damage.getAmount() > 0) return false;

        // Roll chance
        if (Math.random() > STEAL_CHANCE) return false;

        try {
            PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
            if (victimData == null) return false;

            long victimMoney = victimData.getMoney();
            if (victimMoney <= 0) return false;

            long stolenAmount = (long)(victimMoney * GOLD_STEAL_PERCENT);
            if (stolenAmount <= 0) return false;

            victimData.removeMoney(stolenAmount);
            attackerData.addMoney(stolenAmount);

            Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
            if (attackerPlayer != null) {
                attackerPlayer.getPlayerRef().sendMessage(
                        Message.raw("[Luxure] Vous avez volé " + stolenAmount + " or à votre victime !").color(Color.MAGENTA));
            }

            return true;
        } catch (Exception e) {
            EldaniorLogger.error("Luxure", e);
            return false;
        }
    }
}
