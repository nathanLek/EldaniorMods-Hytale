package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.config.configs.StatConfig;

/** Diligence (Metatron, Eglise) : +100 % de vitesse ; +20 % d'XP (DeathXPSystem) et maitrise x5 (CombatStatsSystem). */
public class Diligence implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat, int rank, boolean mastered) {
        return stat == StatConfig.AGILITY_SPEED ? 2.0f : 1f;
    }
}
