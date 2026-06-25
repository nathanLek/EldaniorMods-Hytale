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
 * Orgueil — +10% puissance de combat par item équipé. 7 items = +70%.
 * PK only, PVP/Duel only. Aucun bonus permanent.
 */
public class Orgueil implements IPassiveCombatSkill {

    private static final float BONUS_PER_ITEM = 0.10f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return false;

        int equippedCount = 0;
        try {
            var hotbar = attackerPlayer.getInventory().getHotbar();
            for (short i = 0; i < 9; i++) {
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
