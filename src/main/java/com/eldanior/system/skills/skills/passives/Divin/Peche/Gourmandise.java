package com.eldanior.system.skills.skills.passives.Divin.Peche;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.skills.SkillManager;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Gourmandise (Gluttony) — 5% de chance de copier 1 compétence du joueur tué.
 * Cooldown 2 jours. Cherche un skill compatible avec la classe du joueur.
 * Si c'est une évolution d'un skill possédé → le fait évoluer.
 * Si aucun skill compatible → "Échec de la Prédation".
 * PK only, PVP/Duel only.
 */
public class Gourmandise implements IPassiveCombatSkill {

    private static final float COPY_CHANCE = 0.10f;
    private static final float COOLDOWN_SECONDS = 172800f; // 2 jours

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store,
                           Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // PVP only
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        if (victimPlayer == null) return false;

        // Check if this hit will kill the target
        EntityStatMap victimStats = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStats == null) return false;
        EntityStatValue victimHealth = victimStats.get(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth());
        if (victimHealth == null || victimHealth.get() - damage.getAmount() > 0) return false;

        // Roll chance
        if (Math.random() > COPY_CHANCE) return false;

        try {
            PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
            if (victimData == null) return false;

            Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
            if (attackerPlayer == null) return false;

            // Get victim's unlocked skills
            List<String> victimSkills = victimData.getUnlockedSkills();
            List<String> attackerSkills = attackerData.getUnlockedSkills();

            // Find skills the attacker doesn't have
            List<String> candidateSkills = new ArrayList<>();
            for (String skillId : victimSkills) {
                // Skip divine skills
                if (skillId.startsWith("PECHE_") || skillId.startsWith("ANGE_")) continue;
                if (!attackerSkills.contains(skillId)) {
                    candidateSkills.add(skillId);
                }
            }

            if (candidateSkills.isEmpty()) {
                attackerPlayer.getPlayerRef().sendMessage(
                        Message.raw("§c[Gourmandise] §fÉchec de la Prédation — aucune compétence compatible.").color(Color.RED));
                return true;
            }

            // Pick a random compatible skill
            String copiedSkillId = candidateSkills.get((int)(Math.random() * candidateSkills.size()));

            // Check if it's an evolution of a skill the attacker already has
            var skillModel = SkillManager.getSkillFromId(copiedSkillId);
            if (skillModel.isPresent() && skillModel.get().levelDown() != null) {
                for (String lowerSkill : skillModel.get().levelDown()) {
                    if (attackerSkills.contains(lowerSkill)) {
                        // Evolve!
                        attackerData.removeSkill(lowerSkill);
                        attackerData.learnSkill(copiedSkillId);
                        attackerPlayer.getPlayerRef().sendMessage(
                                Message.raw("§5[Gourmandise] §fPrédation réussie ! Évolution : §e" + copiedSkillId).color(Color.MAGENTA));
                        return true;
                    }
                }
            }

            // Learn new skill
            attackerData.learnSkill(copiedSkillId);
            attackerPlayer.getPlayerRef().sendMessage(
                    Message.raw("§5[Gourmandise] §fPrédation réussie ! Compétence copiée : §e" + copiedSkillId).color(Color.MAGENTA));
            return true;

        } catch (Exception e) {
            EldaniorLogger.error("Gourmandise", e);
            return false;
        }
    }
}
