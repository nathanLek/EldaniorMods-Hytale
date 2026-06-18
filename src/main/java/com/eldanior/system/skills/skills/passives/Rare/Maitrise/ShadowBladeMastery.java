package com.eldanior.system.skills.skills.passives.Rare.Maitrise;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class ShadowBladeMastery implements IPassiveCombatSkill {
    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (attackerRef == null) return;
        Player player = store.getComponent(attackerRef, Player.getComponentType());
        if (player == null) return;
        ItemStack mainHandItem = player.getInventory().getActiveHotbarItem();
        if (mainHandItem == null) return;
        if (!mainHandItem.getItemId().toLowerCase().contains("dagger")) return;
        damage.setAmount(damage.getAmount() * 1.15f);
    }
}