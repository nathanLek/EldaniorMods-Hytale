package com.eldanior.system.skills.skills.passives.Common.Regeneration;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NaturalRecovery implements IPassiveCombatSkill {

    private static final float REGEN_MULTIPLIER = 1.20f;
    private static final float REGEN_MULTIPLIER_MASTERED = 1.32f;

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        victimData.setLastDamageTakenTime(System.currentTimeMillis());
        return false;
    }

    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return REGEN_MULTIPLIER;
        }
        return 1.0f;
    }
    // Progression gérée par MovementTrackingSystem (regen vie)
}
