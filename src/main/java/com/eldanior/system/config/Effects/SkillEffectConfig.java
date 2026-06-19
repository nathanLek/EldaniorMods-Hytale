package com.eldanior.system.config.Effects;

import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.config.EldaniorLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mapping centralise : PassiveSkill -> EntityEffect ID de Hytale.
 * Quand une competence proc, l'effet visuel associe est joue automatiquement.
 */
public class SkillEffectConfig {

    // Effet sur l'ATTAQUANT quand il proc
    private static final Map<PassiveSkill, String> ATTACKER_EFFECTS = new HashMap<>();
    // Effet sur la VICTIME quand le skill proc
    private static final Map<PassiveSkill, String> VICTIM_EFFECTS = new HashMap<>();

    static {
        // Stoneskin — uniquement sur la competence Stone Skin
        ATTACKER_EFFECTS.put(PassiveSkill.STONE_SKIN, "Stoneskin");

        // Burn — Famille Drakenhart (feu draconique sur la victime)
        VICTIM_EFFECTS.put(PassiveSkill.FAMILY_DRAGON_FURY, "Burn");

        // Intangible_Dark — ombre
        ATTACKER_EFFECTS.put(PassiveSkill.SHADOW_DODGE, "Intangible_Dark");
        VICTIM_EFFECTS.put(PassiveSkill.FAMILY_SHADOW_STRIKE, "Intangible_Dark");

        // Dodge_Invulnerability — esquives ultimes
        ATTACKER_EFFECTS.put(PassiveSkill.FATE_DODGE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.TIME_DODGE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.REALITY_DODGE, "Dodge_Invulnerability");

        // Immune — Dyna Aegis
        ATTACKER_EFFECTS.put(PassiveSkill.DYNA_AEGIS, "Immune");

        // Mana_Drain — drain sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.SPIRITUAL_SIPHON, "Mana_Drain");
        VICTIM_EFFECTS.put(PassiveSkill.SPIRIT_DRAIN, "Mana_Drain");
        VICTIM_EFFECTS.put(PassiveSkill.SOUL_STEALER, "Mana_Drain");

        // Slow — ralentissement sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.MAUVAIS_PRESAGE, "Slow");
        VICTIM_EFFECTS.put(PassiveSkill.RELENTLESS_HUNT, "Slow");

        // Stun — stun sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.CRUSHING_PRESSURE, "Stun");
        VICTIM_EFFECTS.put(PassiveSkill.ANNIHILATING_PRESSURE, "Stun");
        VICTIM_EFFECTS.put(PassiveSkill.SOUL_CRUSHING_PRESSURE, "Stun");
        VICTIM_EFFECTS.put(PassiveSkill.KODA_JUDGMENT, "Stun");
        VICTIM_EFFECTS.put(PassiveSkill.JUDGMENT_OF_GENESIS, "Stun");

        // Poison — sur la victime (scale par tier)
        VICTIM_EFFECTS.put(PassiveSkill.CRIMSON_BLADE, "Poison_T1");
        VICTIM_EFFECTS.put(PassiveSkill.HAUNTING_THRUST, "Poison_T2");
        VICTIM_EFFECTS.put(PassiveSkill.FAMILY_SHADOW_STRIKE, "Poison_T3");

        // Freeze — Famille Frostguard
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_FROST_RESILIENCE, "Freeze");

        // Flame_Staff_Burn — brulure magique sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.SPELLBLADE, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_STRIKE, "Flame_Staff_Burn");

        // Bomb_Explode_Stun — explosion sur la victime
        VICTIM_EFFECTS.put(PassiveSkill.ANNIHILATOR_STRIKE, "Bomb_Explode_Stun");

        // Mana_Regen_Low — regen mana faible
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_FONT, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_STREAM, "Mana_Regen_Low");

        // Mana_Regen — regen mana standard
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_RIVER, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_BARRIER, "Mana_Regen");

        // Mana_Regen_High — regen mana forte
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_OCEAN, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_HEART, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_INFINITY, "Mana_Regen_High");

        // Mana_High — mana eleve (aura)
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_FORTRESS, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.MANA_CITADEL, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.EXPANDED_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.BRILLIANT_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.GENIUS_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.INFINITE_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_MIND, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_MIND, "Mana_High");

        // Food_Health_Regen_Tiny — micro regen vie
        ATTACKER_EFFECTS.put(PassiveSkill.VITAL_RECOVERY, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.NATURAL_RECOVERY, "Food_Health_Regen_Tiny");

        // Food_Health_Regen_Large — grosse regen vie
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_PHOENIX_BLOOD, "Food_Health_Regen_Large");

        // Food_Health_Boost_Large — gros boost HP
        ATTACKER_EFFECTS.put(PassiveSkill.GOD_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_CONSTITUTION, "Food_Health_Boost_Large");
    }

    // --- Validation results (populated at startup) ---
    private static int totalSkills = 0;
    private static int skillsWithEffect = 0;
    private static int skillsWithoutEffect = 0;
    private static final List<String> missingAssets = new ArrayList<>();
    private static final List<String> unmappedSkillNames = new ArrayList<>();
    private static boolean validated = false;

    /**
     * Valide tous les mappings au demarrage du serveur.
     * - WARNING pour les skills sans effet visuel
     * - ERROR pour les assets references mais introuvables
     */
    public static void validateAtStartup() {
        missingAssets.clear();
        unmappedSkillNames.clear();

        PassiveSkill[] allSkills = PassiveSkill.values();
        totalSkills = allSkills.length;

        // Collect all unique effect IDs referenced
        Set<String> allEffectIds = new HashSet<>();
        allEffectIds.addAll(ATTACKER_EFFECTS.values());
        allEffectIds.addAll(VICTIM_EFFECTS.values());

        // Check each referenced effect asset exists
        for (String effectId : allEffectIds) {
            if (EffectsManager.getEffectAsset(effectId) == null) {
                missingAssets.add(effectId);
                EldaniorLogger.error("SkillEffectConfig: Asset introuvable: \"" + effectId + "\"");
            }
        }

        // Check each skill for effect mappings
        Set<PassiveSkill> mappedSkills = new HashSet<>();
        mappedSkills.addAll(ATTACKER_EFFECTS.keySet());
        mappedSkills.addAll(VICTIM_EFFECTS.keySet());
        skillsWithEffect = mappedSkills.size();
        skillsWithoutEffect = totalSkills - skillsWithEffect;

        // Log unmapped skills as warnings (group by first word for readability)
        for (PassiveSkill skill : allSkills) {
            if (!mappedSkills.contains(skill)) {
                unmappedSkillNames.add(skill.name());
            }
        }

        validated = true;

        // Summary log
        int coveragePct = totalSkills > 0 ? (skillsWithEffect * 100 / totalSkills) : 0;
        EldaniorLogger.info("SkillEffectConfig: Validation terminee — "
                + skillsWithEffect + "/" + totalSkills + " skills avec effet (" + coveragePct + "% couverture)"
                + (missingAssets.isEmpty() ? "" : " | " + missingAssets.size() + " asset(s) introuvable(s)"));

        if (!missingAssets.isEmpty()) {
            EldaniorLogger.error("SkillEffectConfig: Assets manquants: " + missingAssets);
        }
        if (skillsWithoutEffect > 0) {
            EldaniorLogger.warn("SkillEffectConfig: " + skillsWithoutEffect + " skills sans effet visuel");
        }
    }

    /** Couverture pour le rapport admin */
    public static int getTotalSkills() { return totalSkills; }
    public static int getSkillsWithEffect() { return skillsWithEffect; }
    public static int getSkillsWithoutEffect() { return skillsWithoutEffect; }
    public static List<String> getMissingAssets() { return missingAssets; }
    public static List<String> getUnmappedSkillNames() { return unmappedSkillNames; }
    public static boolean isValidated() { return validated; }
    public static int getMappingCount() { return ATTACKER_EFFECTS.size() + VICTIM_EFFECTS.size(); }

    /**
     * Retourne l'effet a jouer sur l'attaquant quand ce skill proc.
     */
    public static String getAttackerEffect(PassiveSkill skill) {
        return ATTACKER_EFFECTS.get(skill);
    }

    /**
     * Retourne l'effet a jouer sur la victime quand ce skill proc.
     */
    public static String getVictimEffect(PassiveSkill skill) {
        return VICTIM_EFFECTS.get(skill);
    }

    /**
     * Applique les effets visuels d'un skill qui vient de proc.
     */
    public static void applySkillEffects(PassiveSkill skill,
                                          com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> attackerRef,
                                          com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> victimRef,
                                          com.hypixel.hytale.component.ComponentAccessor<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store) {
        try {
            String attackerEffect = ATTACKER_EFFECTS.get(skill);
            if (attackerEffect != null && attackerRef != null) {
                EffectsManager.applyEffect(attackerRef, attackerEffect, store);
            }

            String victimEffect = VICTIM_EFFECTS.get(skill);
            if (victimEffect != null && victimRef != null) {
                EffectsManager.applyEffect(victimRef, victimEffect, store);
            }
        } catch (Exception e) { EldaniorLogger.error("SkillEffectConfig", e); }
    }
}
