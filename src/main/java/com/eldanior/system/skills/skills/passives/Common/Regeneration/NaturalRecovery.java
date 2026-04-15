package com.eldanior.system.skills.skills.passives.Common.Regeneration;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NaturalRecovery implements IPassiveCombatSkill {

    private static final long COOLDOWN_MS = 5000; // 5 secondes hors combat

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // Quand on se fait toucher, on reset le timer hors-combat
        victimData.setLastDamageTakenTime(System.currentTimeMillis());
    }

    @Override
    public float getRegenMultiplier(com.eldanior.system.config.configs.StatConfig stat) {
        if (stat == com.eldanior.system.config.configs.StatConfig.VITALITY) {
            return 1.5f; // +50% de regen de vie naturelle
        }
        return 1.0f;
    }
}