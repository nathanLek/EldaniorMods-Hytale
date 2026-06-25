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
 * Humilité (Michaël) — Moins le joueur possède d'armure, plus ses dégâts et sa santé augmentent.
 * 0 armure = +200%. 1 pièce = +150%. 2 pièces = +100%. 3 pièces = +50%. 4 pièces = 0%.
 * Église RELIGIEUX+ only.
 */
public class Humilite implements IPassiveCombatSkill {

    private static final float MAX_BONUS = 2.0f; // +200% at 0 armor
    private static final int MAX_ARMOR_PIECES = 4;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        int armorCount = countArmorPieces(attackerPlayer);
        if (armorCount >= MAX_ARMOR_PIECES) return false;

        float bonusPercent = MAX_BONUS * (1.0f - ((float) armorCount / MAX_ARMOR_PIECES));
        damage.setAmount(damage.getAmount() * (1.0f + bonusPercent));
        return true;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;

        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        int armorCount = countArmorPieces(victimPlayer);
        if (armorCount >= MAX_ARMOR_PIECES) return false;

        float bonusPercent = MAX_BONUS * (1.0f - ((float) armorCount / MAX_ARMOR_PIECES));
        float reductionFactor = 1.0f / (1.0f + bonusPercent);
        damage.setAmount(damage.getAmount() * reductionFactor);
        return true;
    }

    private int countArmorPieces(Player player) {
        int count = 0;
        try {
            var armorContainer = player.getInventory().getArmor();
            if (armorContainer != null) {
                for (short i = 0; i < 4; i++) {
                    ItemStack item = armorContainer.getItemStack(i);
                    if (item != null && !item.isEmpty()) count++;
                }
            }
        } catch (Exception ignored) {}
        return count;
    }
}
