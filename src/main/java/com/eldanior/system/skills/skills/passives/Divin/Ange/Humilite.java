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
 * Humilité (Michaël) — Moins le joueur possède d'items dans sa hotbar, plus ses dégâts et sa santé augmentent.
 * 0 items = +200%. Église RELIGIEUX+ only.
 */
public class Humilite implements IPassiveCombatSkill {

    private static final float MAX_BONUS = 2.0f; // +200% at 0 items
    private static final int HOTBAR_SIZE = 9;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        int itemCount = countHotbarItems(attackerPlayer);
        if (itemCount >= HOTBAR_SIZE) return false;

        float bonusPercent = MAX_BONUS * (1.0f - ((float) itemCount / HOTBAR_SIZE));
        damage.setAmount(damage.getAmount() * (1.0f + bonusPercent));
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;

        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        int itemCount = countHotbarItems(victimPlayer);
        if (itemCount >= HOTBAR_SIZE) return false;

        float bonusPercent = MAX_BONUS * (1.0f - ((float) itemCount / HOTBAR_SIZE));
        float reductionFactor = 1.0f / (1.0f + bonusPercent);
        damage.setAmount(damage.getAmount() * reductionFactor);
        return true;
    }

    private int countHotbarItems(Player player) {
        int count = 0;
        try {
            var hotbar = player.getInventory().getHotbar();
            for (short i = 0; i < HOTBAR_SIZE; i++) {
                ItemStack item = hotbar.getItemStack(i);
                if (item != null && !item.isEmpty()) count++;
            }
        } catch (Exception ignored) {}
        return count;
    }
}
