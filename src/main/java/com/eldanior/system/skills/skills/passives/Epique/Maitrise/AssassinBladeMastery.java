package com.eldanior.system.skills.skills.passives.Epique.Maitrise;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class AssassinBladeMastery implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (attackerRef == null) return false;
        Player player = store.getComponent(attackerRef, Player.getComponentType());
        if (player == null) return false;
        ItemStack mainHandItem = player.getInventory().getActiveHotbarItem();
        if (mainHandItem == null || !mainHandItem.getItemId().toLowerCase().contains("dagger")) return false;
        damage.setAmount(damage.getAmount() * 1.20f);
        return false;
    }
}