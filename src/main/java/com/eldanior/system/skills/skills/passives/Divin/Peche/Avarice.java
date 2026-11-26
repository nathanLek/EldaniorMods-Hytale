package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Avarice (Peche, PK) : +15 % de chance de butin ; en tuant un joueur, 25 % de chance de lui voler un objet de sa barre
 * d'action (recharge d'un jour : PassiveSkill.PECHE_AVARICE). */
public class Avarice implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat, int rank, boolean mastered) {
        return stat == StatConfig.LUCK_LOOT ? 15f : 0f;
    }

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (!PouvoirsDivins.killsPlayer(damage, victimRef, store) || ThreadLocalRandom.current().nextFloat() >= 0.25f) return false;
        try {
            Player victim = store.getComponent(victimRef, Player.getComponentType());
            Player attacker = store.getComponent(attackerRef, Player.getComponentType());
            if (victim == null || attacker == null) return false;
            var hotbar = victim.getInventory().getHotbar();
            List<Short> slots = new ArrayList<>();
            for (short i = 0; i < hotbar.getCapacity(); i++) {
                ItemStack it = hotbar.getItemStack(i);
                if (it != null && !it.isEmpty()) slots.add(i);
            }
            if (slots.isEmpty()) return false;
            short slot = slots.get(ThreadLocalRandom.current().nextInt(slots.size()));
            ItemStack stolen = hotbar.getItemStack(slot);
            if (stolen == null) return false;
            hotbar.removeItemStackFromSlot(slot, stolen.getQuantity(), true, false);
            attacker.getInventory().getCombinedHotbarFirst().addItemStack(stolen);
            attacker.getPlayerRef().sendMessage(Message.raw("[Avarice] Vous avez volé un objet à votre victime.").color(Color.MAGENTA));
            return true;
        } catch (Exception e) {
            EldaniorLogger.error("Avarice", e);
            return false;
        }
    }
}
