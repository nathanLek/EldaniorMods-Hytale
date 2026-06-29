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

        // Flame_Staff_Burn — retire : ces skills ne sont pas lies au feu
        // VICTIM_EFFECTS.put(PassiveSkill.SPELLBLADE, "Flame_Staff_Burn");
        // VICTIM_EFFECTS.put(PassiveSkill.ARCANE_STRIKE, "Flame_Staff_Burn");

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

        // =====================================================================
        // ELD-12 : Mappings des skills manquants vers des effets visuels
        // =====================================================================

        // --- COMMON ATTACK (effet sur victime : dégâts physiques) ---
        // Flame_Staff_Burn retire de ces skills — pas lies au feu
        VICTIM_EFFECTS.put(PassiveSkill.PRESSURE_POINT, "Bomb_Explode_Stun");

        // --- ATTACK tiers (effet sur victime) ---
        VICTIM_EFFECTS.put(PassiveSkill.SEISMIC_STRIKE, "Bomb_Explode_Stun");
        VICTIM_EFFECTS.put(PassiveSkill.PHANTOM_STRIKE, "Bomb_Explode_Stun");

        // --- UNCOMMON ATTACK ---
        VICTIM_EFFECTS.put(PassiveSkill.VITAL_PRESSURE, "Bomb_Explode_Stun");

        // --- DIVIN ATTACK ---
        VICTIM_EFFECTS.put(PassiveSkill.CREATOR_EDGE, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.CREATOR_SWIFTNESS, "Flame_Staff_Burn");

        // --- DEFENSE (effet sur attaquant : buff defensif) ---
        ATTACKER_EFFECTS.put(PassiveSkill.BRONZE_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.OBSIDIAN_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.DIAMOND_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_AEGIS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_RESOLVE, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.MINOR_PARRY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.HUNTER_GUARD, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.STURDY_BODY, "Stoneskin");

        // --- UNCOMMON DEFENSE ---
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_RESOLVE, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.EXPERT_PARRY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.BEAST_GUARD, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_BODY, "Stoneskin");

        // --- RARE DEFENSE ---
        ATTACKER_EFFECTS.put(PassiveSkill.TITAN_RESOLVE, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.MASTER_PARRY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.MONSTER_SLAYER_GUARD, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_BODY, "Stoneskin");

        // --- EPIQUE DEFENSE ---
        ATTACKER_EFFECTS.put(PassiveSkill.GOD_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.PERFECT_PARRY, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DRAGON_SLAYER_GUARD, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DIAMOND_BODY, "Immune");

        // --- UNIQUE DEFENSE ---
        ATTACKER_EFFECTS.put(PassiveSkill.IMMORTAL_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_PARRY, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_BODY, "Immune");

        // --- LEGENDAIRE DEFENSE ---
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNITY_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_PARRY, "Immune");

        // --- DIVIN DEFENSE ---
        ATTACKER_EFFECTS.put(PassiveSkill.ABSOLUTE_RESOLVE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_PARRY, "Immune");

        // --- RESISTANCE (effet sur attaquant : reduction dégâts) ---
        ATTACKER_EFFECTS.put(PassiveSkill.TENACITY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.ADAPTIVE_SHIELD, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.PAIN_TOLERANCE, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.HARDENING, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_NERVES, "Stoneskin");

        // --- UNCOMMON RESISTANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.UNYIELDING, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.REACTIVE_BULWARK, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.BATTLE_SCARS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.FORTIFICATION, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_WILL, "Stoneskin");

        // --- RARE RESISTANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.UNBREAKABLE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.ADAMANTINE_BULWARK, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_VETERAN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_FORTIFICATION, "Immune");

        // --- EPIQUE RESISTANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.INVINCIBLE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_BULWARK, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_LEGEND, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.MYTHRIL_FORTIFICATION, "Immune");

        // --- UNIQUE RESISTANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.IMMORTAL_ABSOLUTE, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_BULWARK, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_FORTIFICATION, "Immune");

        // --- LEGENDAIRE RESISTANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_IMMORTALITY, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_BULWARK, "Immune");

        // --- DIVIN RESISTANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.TRUE_IMMORTALITY, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_BULWARK, "Immune");

        // --- VIE / CONSTITUTION (boost HP sur attaquant) ---
        ATTACKER_EFFECTS.put(PassiveSkill.ROBUST_CONSTITUTION, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.LIFE_FORCE, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.VITAL_BLOOD, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_OAK, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.PERSEVERANCE, "Food_Health_Regen_Tiny");

        // --- UNCOMMON VIE ---
        ATTACKER_EFFECTS.put(PassiveSkill.STEEL_CONSTITUTION, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.OVERFLOWING_LIFE, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.ENRICHED_BLOOD, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_IRON, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.UNDYING, "Food_Health_Regen_Tiny");

        // --- RARE VIE ---
        ATTACKER_EFFECTS.put(PassiveSkill.TITAN_CONSTITUTION, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.BURSTING_LIFE, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.ANCIENT_BLOOD, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_STEEL, "Food_Health_Boost_Large");

        // --- EPIQUE VIE ---
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_LIFE, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.DRAGON_BLOOD, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_DIAMOND, "Food_Health_Boost_Large");

        // --- UNIQUE VIE ---
        ATTACKER_EFFECTS.put(PassiveSkill.INFINITE_LIFE, "Food_Health_Boost_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_ETERNITY, "Food_Health_Boost_Large");

        // --- LEGENDAIRE VIE ---
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_GENESIS, "Food_Health_Boost_Large");

        // --- DIVIN VIE ---
        ATTACKER_EFFECTS.put(PassiveSkill.HEART_OF_CREATION, "Food_Health_Boost_Large");

        // --- REGENERATION (sur attaquant) ---
        ATTACKER_EFFECTS.put(PassiveSkill.CELLULAR_REGENERATION, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.ACTIVE_BREATHING, "Food_Health_Regen_Tiny");

        // --- UNCOMMON REGENERATION ---
        ATTACKER_EFFECTS.put(PassiveSkill.TROLL_BLOOD, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.IRON_LUNGS, "Food_Health_Regen_Tiny");

        // --- RARE REGENERATION ---
        ATTACKER_EFFECTS.put(PassiveSkill.HYDRA_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.ADAMANTINE_LUNGS, "Food_Health_Regen_Tiny");

        // --- EPIQUE REGENERATION ---
        ATTACKER_EFFECTS.put(PassiveSkill.PHOENIX_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.MYTHRIL_LUNGS, "Food_Health_Regen_Tiny");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_VAMPIRISM, "Mana_Regen");

        // --- UNIQUE REGENERATION ---
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_VAMPIRISM, "Mana_Regen_High");

        // --- LEGENDAIRE REGENERATION ---
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.INFINITE_VAMPIRISM, "Mana_Regen_High");

        // --- DIVIN REGENERATION ---
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_BLOOD, "Food_Health_Regen_Large");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_VAMPIRISM, "Mana_Regen_High");

        // --- AGILITE (sur attaquant : esquive/vitesse) ---
        ATTACKER_EFFECTS.put(PassiveSkill.WIND_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.LIGHT_REFLEXES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.ELDANIOR_SUPPLENESS, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.KEEN_SENSES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.TONOSQUIVE, "Dodge_Invulnerability");

        // --- UNCOMMON AGILITE ---
        ATTACKER_EFFECTS.put(PassiveSkill.GALE_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.THUNDER_REFLEXES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.CATLIKE_POISE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.RAZOR_SENSES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.MARATHON_RUNNER, "Dodge_Invulnerability");

        // --- RARE AGILITE ---
        ATTACKER_EFFECTS.put(PassiveSkill.STORM_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.LIGHTNING_REFLEXES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.ACROBATIC_POISE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.DEADLY_PRECISION, "Dodge_Invulnerability");

        // --- EPIQUE AGILITE ---
        ATTACKER_EFFECTS.put(PassiveSkill.VOID_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_REFLEXES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.GRAVITY_DEFIANCE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.FATAL_PRECISION, "Dodge_Invulnerability");

        // --- UNIQUE AGILITE ---
        ATTACKER_EFFECTS.put(PassiveSkill.DIMENSIONAL_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_REFLEXES, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.ABSOLUTE_PRECISION, "Dodge_Invulnerability");

        // --- LEGENDAIRE AGILITE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.OMNISCIENT_PRECISION, "Dodge_Invulnerability");

        // --- DIVIN AGILITE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_STEP, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_PRECISION, "Dodge_Invulnerability");

        // --- ENDURANCE (sur attaquant) ---
        ATTACKER_EFFECTS.put(PassiveSkill.TIRELESS_BREATH, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.THICK_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.SOLID_STANCE, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.COMBAT_VIGOR, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.SECOND_WIND, "Stoneskin");

        // --- UNCOMMON ENDURANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.LUNGS_OF_STEEL, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.ARMORED_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.UNMOVABLE_MOUNTAIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.BATTLE_FRENZY, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.SURVIVOR_SPIRIT, "Stoneskin");

        // --- RARE ENDURANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.DRAGON_LUNGS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.FORTIFIED_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.LIVING_FORTRESS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_FRENZY, "Stoneskin");

        // --- EPIQUE ENDURANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.TITAN_LUNGS, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.ADAMANTINE_SKIN, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.ETERNAL_FORTRESS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.BLOOD_FRENZY, "Stoneskin");

        // --- UNIQUE ENDURANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_LUNGS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_FORTRESS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.RAGE_FRENZY, "Stoneskin");

        // --- LEGENDAIRE ENDURANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_LUNGS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CELESTIAL_FORTRESS, "Immune");

        // --- DIVIN ENDURANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_LUNGS, "Immune");
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_FORTRESS, "Immune");

        // --- MAGIQUE (ceux non mappes) ---
        ATTACKER_EFFECTS.put(PassiveSkill.AWAKENED_MIND, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.OVERFLOWING_POWER, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.MYSTIC_VEIL, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_SHIELD, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.UNLEASHED_MAGIC, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.ASTRAL_CLOAK, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.PURE_MAGIC, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ARCANE_SUPREMACY, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ABSOLUTE_SUPREMACY, "Mana_High");

        // Magique offensifs (effet sur victime)
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_DEVASTATION, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_ANNIHILATION, "Flame_Staff_Burn");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_OBLIVION, "Bomb_Explode_Stun");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_GENESIS, "Bomb_Explode_Stun");
        VICTIM_EFFECTS.put(PassiveSkill.ARCANE_CREATION, "Bomb_Explode_Stun");

        // --- CHANCE (sur attaquant : buff chanceux) ---
        ATTACKER_EFFECTS.put(PassiveSkill.LUCKY_STRIKE, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.TREASURE_HUNTER, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.GOOD_OMEN, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.MIRACLE_DODGE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.FORTUNE_COINS, "Mana_Regen_Low");

        // --- UNCOMMON CHANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CRITICAL_LUCK, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.RELIC_HUNTER, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.FATED_OMEN, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.PHANTOM_DODGE, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.GOLDEN_TOUCH, "Mana_Regen_Low");

        // --- RARE CHANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.DESTINY_STRIKE, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.ARTIFACT_HUNTER, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.PROPHECY_OMEN, "Mana_Regen");

        // --- EPIQUE CHANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.DIVINE_STRIKE, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.LEGEND_HUNTER, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_OMEN, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.DIMENSIONAL_DODGE, "Dodge_Invulnerability");

        // --- UNIQUE CHANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.COSMIC_STRIKE, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.MYTH_HUNTER, "Mana_High");

        // --- LEGENDAIRE CHANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_STRIKE, "Mana_High");

        // --- DIVIN CHANCE ---
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_STRIKE, "Mana_High");

        // --- DETECTION (sur attaquant : effet subtil) ---
        ATTACKER_EFFECTS.put(PassiveSkill.DETECTIONOFVITALPOINTS, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.UNIVERSAL_DETECTION, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.EAGLE_EYE, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.SURVIVAL_INSTINCT, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.NIGHT_VISION, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.SIXTH_SENSE, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.TRACKER, "Mana_Regen_Low");

        // --- UNCOMMON DETECTION ---
        ATTACKER_EFFECTS.put(PassiveSkill.HAWK_EYE, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.DANGER_SENSE, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.DARK_VISION, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.PSYCHIC_AWARENESS, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.MASTER_TRACKER, "Mana_Regen_Low");

        // --- RARE DETECTION ---
        ATTACKER_EFFECTS.put(PassiveSkill.EAGLE_VISION, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.COMBAT_INTUITION, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.ABYSSAL_VISION, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.MIND_READER, "Mana_Regen");

        // --- EPIQUE DETECTION ---
        ATTACKER_EFFECTS.put(PassiveSkill.OMNISCIENT_VISION, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.WAR_PROPHECY, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.VOID_SIGHT, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.SOUL_READER, "Mana_Regen");

        // --- UNIQUE DETECTION ---
        ATTACKER_EFFECTS.put(PassiveSkill.ALL_SEEING_EYE, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.FATE_VISION, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.TRUE_SIGHT, "Mana_Regen_High");

        // --- LEGENDAIRE DETECTION ---
        ATTACKER_EFFECTS.put(PassiveSkill.GENESIS_VISION, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.OMNI_SIGHT, "Mana_High");

        // --- DIVIN DETECTION ---
        ATTACKER_EFFECTS.put(PassiveSkill.CREATOR_VISION, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.ABSOLUTE_SIGHT, "Mana_High");

        // --- FAMILLE NOBLE (ceux non mappes) ---
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_ROYAL_AUTHORITY, "Mana_High");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_DIVINE_LIGHT, "Mana_Regen_High");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_GOLDEN_FORTUNE, "Mana_Regen");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_IRON_WILL, "Stoneskin");
        ATTACKER_EFFECTS.put(PassiveSkill.FAMILY_STORM_VIGOR, "Dodge_Invulnerability");

        // --- GUERRIER de base (ceux non mappes) ---
        ATTACKER_EFFECTS.put(PassiveSkill.SWORD_MASTERY, "Flame_Staff_Burn");
        ATTACKER_EFFECTS.put(PassiveSkill.ATHLETICISM, "Dodge_Invulnerability");
        ATTACKER_EFFECTS.put(PassiveSkill.MANAWELL, "Mana_Regen_Low");
        ATTACKER_EFFECTS.put(PassiveSkill.VOL, "Dodge_Invulnerability");
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
