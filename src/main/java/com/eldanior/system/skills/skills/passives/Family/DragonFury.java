package com.eldanior.system.skills.skills.passives.Family;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Famille Drakenhart (Marquis) — Fureur Draconique
 * Bonus degats.
 * Nv1(lv1): +3% | Nv2(lv100): +6% | Nv3(lv300): +10% | Nv4(lv500): +15% | Nv5(lv800): +20%
 */
public class DragonFury implements IPassiveCombatSkill {

    private float getBonus(int playerLevel) {
        if (playerLevel >= 800) return 0.20f;
        if (playerLevel >= 500) return 0.15f;
        if (playerLevel >= 300) return 0.10f;
        if (playerLevel >= 100) return 0.06f;
        return 0.03f;
    }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;
        float bonus = getBonus(attackerData.getLevel());
        damage.setAmount(damage.getAmount() * (1.0f + bonus));
    }
}
