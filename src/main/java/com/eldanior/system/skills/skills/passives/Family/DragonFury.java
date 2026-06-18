package com.eldanior.system.skills.skills.passives.Family;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Famille Drakenhart (Marquis) — Fureur Draconique
 * Effet visuel CrowDraconic_Effect sur l'attaquant (via SkillEffectConfig).
 */
public class DragonFury implements IPassiveCombatSkill {

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // Effet visuel uniquement (applique par SkillEffectConfig)
        return false;
    }
}