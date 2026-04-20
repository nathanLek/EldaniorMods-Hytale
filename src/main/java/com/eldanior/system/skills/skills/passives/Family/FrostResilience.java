package com.eldanior.system.skills.skills.passives.Family;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Famille Frostguard (Duc) — Resilience du Givre
 * Reduction de degats.
 * Nv1(lv1): -2% | Nv2(lv100): -5% | Nv3(lv300): -8% | Nv4(lv500): -12% | Nv5(lv800): -16%
 */
public class FrostResilience implements IPassiveCombatSkill {

    private float getReduction(int playerLevel) {
        if (playerLevel >= 800) return 0.16f;
        if (playerLevel >= 500) return 0.12f;
        if (playerLevel >= 300) return 0.08f;
        if (playerLevel >= 100) return 0.05f;
        return 0.02f;
    }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;
        float reduction = getReduction(victimData.getLevel());
        damage.setAmount(damage.getAmount() * (1.0f - reduction));
    }
}