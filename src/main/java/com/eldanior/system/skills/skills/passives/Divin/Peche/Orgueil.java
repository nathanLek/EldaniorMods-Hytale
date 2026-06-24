package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Orgueil (Pride) — +10% puissance de combat par item équipé dans la hotbar.
 * Ex: 7 items = +70%. PK only, PVP/Duel only.
 */
public class Orgueil implements IPassiveCombatSkill {

    private static final float BONUS_PER_ITEM = 0.10f;
    private static final int HOTBAR_SIZE = 9;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // PVP only check
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        int equippedCount = 0;
        try {
            var hotbar = attackerPlayer.getInventory().getHotbar();
            for (short i = 0; i < HOTBAR_SIZE; i++) {
                ItemStack item = hotbar.getItemStack(i);
                if (item != null && !item.isEmpty()) equippedCount++;
            }
        } catch (Exception ignored) {}

        if (equippedCount > 0) {
            float multiplier = 1.0f + (equippedCount * BONUS_PER_ITEM);
            damage.setAmount(damage.getAmount() * multiplier);
            return true;
        }
        return false;
    }
}
