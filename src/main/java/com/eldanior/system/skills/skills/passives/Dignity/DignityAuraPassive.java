package com.eldanior.system.skills.skills.passives.Dignity;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Aura de Dignite — Competence Legendaire unique
 * Se debloque automatiquement a 5+ de dignite.
 *
 * Effets :
 * - Bonus de degats proportionnel a la dignite
 * - Reduction de degats recus proportionnelle a la dignite
 * - L'aura ECS (DignityAuraSystem) gere le ralentissement des ennemis proches
 *
 * Scaling :
 *   5 dignite  : +2% degats, -1% degats recus
 *   15 dignite : +5% degats, -3% degats recus
 *   30 dignite : +8% degats, -5% degats recus
 *   50 dignite : +12% degats, -8% degats recus
 *   75 dignite : +18% degats, -12% degats recus
 *   100 dignite: +25% degats, -15% degats recus
 */
public class DignityAuraPassive implements IPassiveCombatSkill {

    private float getDamageBonus(int dignity) {
        if (dignity >= 100) return 0.25f;
        if (dignity >= 75) return 0.18f;
        if (dignity >= 50) return 0.12f;
        if (dignity >= 30) return 0.08f;
        if (dignity >= 15) return 0.05f;
        if (dignity >= 5) return 0.02f;
        return 0f;
    }

    private float getDamageReduction(int dignity) {
        if (dignity >= 100) return 0.15f;
        if (dignity >= 75) return 0.12f;
        if (dignity >= 50) return 0.08f;
        if (dignity >= 30) return 0.05f;
        if (dignity >= 15) return 0.03f;
        if (dignity >= 5) return 0.01f;
        return 0f;
    }

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData,
                         Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return false;
        int dignity = attackerData.getDignity();
        if (dignity < 5) return false;

        float bonus = getDamageBonus(dignity);
        damage.setAmount(damage.getAmount() * (1.0f + bonus));
        return false;
    }

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData defenderData,
                         Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> defenderRef) {
        if (damage.isCancelled()) return false;
        int dignity = defenderData.getDignity();
        if (dignity < 5) return false;

        float reduction = getDamageReduction(dignity);
        damage.setAmount(damage.getAmount() * (1.0f - reduction));
        return false;
    }
}
