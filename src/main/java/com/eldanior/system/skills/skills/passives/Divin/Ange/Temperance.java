package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Tempérance (Uriel) — Combat à mains nues = +200% HP et dégâts.
 * Vérifie que le slot actif de la hotbar est vide.
 * Église RELIGIEUX+ only.
 */
public class Temperance implements IPassiveCombatSkill {

    private static final float UNARMED_MULTIPLIER = 3.0f; // +200% = x3

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        if (isUnarmed(attackerPlayer)) {
            damage.setAmount(damage.getAmount() * UNARMED_MULTIPLIER);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;

        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        if (isUnarmed(victimPlayer)) {
            damage.setAmount(damage.getAmount() / UNARMED_MULTIPLIER);
            return true;
        }
        return false;
    }

    private boolean isUnarmed(Player player) {
        try {
            // Check all hotbar slots - true "unarmed" means nothing in hotbar
            var hotbar = player.getInventory().getHotbar();
            for (short i = 0; i < 9; i++) {
                ItemStack item = hotbar.getItemStack(i);
                if (item != null && !item.isEmpty()) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
