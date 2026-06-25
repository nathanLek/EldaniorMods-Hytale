package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Avarice (Greed) — 5% de chance de voler 1 item de la hotbar du joueur tué.
 * Cooldown 2 jours (temps réel). PK only, PVP/Duel only.
 */
public class Avarice implements IPassiveCombatSkill {

    private static final float STEAL_CHANCE = 0.10f;
    private static final int HOTBAR_SIZE = 9;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // PVP only check
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        // Check if this hit will kill the target
        EntityStatMap victimStats = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStats == null) return false;
        EntityStatValue victimHealth = victimStats.get(DefaultEntityStatTypes.getHealth());
        if (victimHealth == null || victimHealth.get() - damage.getAmount() > 0) return false;

        // Roll chance
        if (Math.random() > STEAL_CHANCE) return false;

        try {
            Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
            if (attackerPlayer == null) return false;

            var victimHotbar = victimPlayer.getInventory().getHotbar();
            var attackerHotbar = attackerPlayer.getInventory().getHotbar();

            // Find non-empty slots in victim's hotbar
            List<Short> nonEmptySlots = new ArrayList<>();
            for (short i = 0; i < HOTBAR_SIZE; i++) {
                ItemStack item = victimHotbar.getItemStack(i);
                if (item != null && !item.isEmpty()) nonEmptySlots.add(i);
            }

            if (nonEmptySlots.isEmpty()) return false;

            // Pick random slot
            short stolenSlot = nonEmptySlots.get((int)(Math.random() * nonEmptySlots.size()));
            ItemStack stolenItem = victimHotbar.getItemStack(stolenSlot);
            if (stolenItem == null) return false;

            // Transfer: remove from victim, give to attacker
            victimHotbar.removeItemStackFromSlot(stolenSlot, 1, true, false);
            attackerHotbar.addItemStack(stolenItem);

            attackerPlayer.getPlayerRef().sendMessage(
                    Message.raw("[Avarice] Vous avez volé un objet à votre victime !").color(Color.MAGENTA));

            return true;
        } catch (Exception e) {
            EldaniorLogger.error("Avarice", e);
            return false;
        }
    }
}
