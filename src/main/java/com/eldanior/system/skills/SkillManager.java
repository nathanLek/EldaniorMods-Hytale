package com.eldanior.system.skills;

import java.util.*;

/**
 * Pages de competence -> passif et rang ; parchemins de Metier -> metier.
 *
 * FICHIER GENERE par scripts/passifs/gen_passifs.py (refonte des passifs du 2026-11-24) : ne pas modifier a la main.
 * Les anciennes pages d'un meme rang (series fusionnees) et les pages Divines des series restent lisibles, mais ne
 * sont plus dans le butin ni dans le mode creatif.
 */
public final class SkillManager {

    private SkillManager() {}

    /** Une page : le passif qu'elle apprend, son rang, les classes qui peuvent la lire ("all" = toutes), et si c'est la page
     *  de reference de ce rang (dans le butin et le mode creatif). */
    public record Page(String itemId, String skillId, int rank, String requiredClass, boolean main) {}

    private static final Map<String, Page> PAGES = new HashMap<>();
    private static final Map<String, String> METIERS = new HashMap<>();
    private static final Set<String> LOST = new HashSet<>();

    public static void init() {
        if (!PAGES.isEmpty()) return;
        lost("skill_page_mauvais_presage");
        lost("skill_page_koda_judgment");
        page("skill_page_opportunist_strike", "OPPORTUNIST_STRIKE", 1, "warrior, assassin, dragon", true);
        page("skill_page_haunting_thrust", "HAUNTING_THRUST", 1, "warrior, assassin, dragon", true);
        page("skill_page_pressure_point", "PRESSURE_POINT", 1, "assassin, archer, dragon", true);
        lost("skill_page_duelist_swiftness");
        page("skill_page_deep_slash", "DEEP_SLASH", 1, "warrior, assassin, dragon", true);
        page("skill_page_instinctive_strike", "INSTINCTIVE_STRIKE", 1, "warrior, assassin, archer, dragon", true);
        page("skill_page_predatory_strike", "INSTINCTIVE_STRIKE", 2, "warrior, assassin, archer, dragon", true);
        page("skill_page_fury_strike", "INSTINCTIVE_STRIKE", 3, "warrior, assassin, archer, dragon", true);
        page("skill_page_seismic_strike", "INSTINCTIVE_STRIKE", 4, "warrior, assassin, archer, dragon", true);
        page("skill_page_phantom_strike", "INSTINCTIVE_STRIKE", 5, "warrior, assassin, archer, dragon", true);
        page("skill_page_annihilator_strike", "INSTINCTIVE_STRIKE", 6, "warrior, assassin, archer, dragon", true);
        page("skill_page_judgment_of_genesis", "INSTINCTIVE_STRIKE", 6, "warrior, assassin, archer, dragon", false);
        lost("skill_page_dyna_aegis");
        page("skill_page_iron_resolve", "IRON_RESOLVE", 1, "warrior, dragon", true);
        page("skill_page_minor_parry", "MINOR_PARRY", 1, "warrior, dragon", true);
        page("skill_page_hunter_guard", "HUNTER_GUARD", 1, "warrior, archer, assassin, dragon", true);
        page("skill_page_sturdy_body", "STURDY_BODY", 1, "warrior, dragon", true);
        page("skill_page_stone_skin", "STONE_SKIN", 1, "warrior, dragon", true);
        page("skill_page_bronze_skin", "STONE_SKIN", 2, "warrior, dragon", true);
        page("skill_page_iron_skin", "STONE_SKIN", 3, "warrior, dragon", true);
        page("skill_page_steel_skin", "STONE_SKIN", 4, "warrior, dragon", true);
        page("skill_page_obsidian_skin", "STONE_SKIN", 5, "warrior, dragon", true);
        page("skill_page_diamond_skin", "STONE_SKIN", 6, "warrior, dragon", true);
        page("skill_page_divine_aegis", "STONE_SKIN", 6, "warrior, dragon", false);
        page("skill_page_wind_step", "WIND_STEP", 1, "novice, warrior, assassin, archer, dragon", true);
        lost("skill_page_light_reflexes");
        page("skill_page_suppleness", "ELDANIOR_SUPPLENESS", 1, "novice, assassin, archer, dragon", true);
        page("skill_page_athleticism", "WIND_STEP", 2, "novice, warrior, assassin, archer, dragon", false);
        lost("skill_page_eagle_eye");
        page("skill_page_tono_squive", "MIRACLE_DODGE", 5, "assassin, dragon", false);
        page("skill_page_survival_instinct", "SURVIVAL_INSTINCT", 1, "warrior, assassin, archer, dragon", true);
        page("skill_page_keen_senses", "LUCKY_STRIKE", 1, "assassin, archer, dragon", false);
        page("skill_page_universal_detection", "SURVIVAL_INSTINCT", 4, "mage, assassin, archer, dragon", false);
        page("skill_page_night_vision", "NIGHT_VISION", 1, "assassin, archer, dragon", true);
        page("skill_page_sixth_sense", "SURVIVAL_INSTINCT", 1, "assassin, archer, dragon", false);
        lost("skill_page_tracker");
        page("skill_page_sword_mastery", "SWORD_MASTERY", 4, "warrior, assassin, dragon", true);
        page("skill_page_fly", "VOL", 6, "mage, dragon", true);
        page("skill_page_mana_well", "AWAKENED_MIND", 4, "mage, dragon", false);
        page("skill_page_awakened_mind", "AWAKENED_MIND", 1, "mage, dragon", true);
        page("skill_page_arcane_strike", "ARCANE_STRIKE", 1, "mage, dragon", true);
        page("skill_page_mana_barrier", "MANA_BARRIER", 1, "mage, dragon", true);
        page("skill_page_overflowing_power", "OVERFLOWING_POWER", 1, "mage, dragon", true);
        page("skill_page_mystic_veil", "MYSTIC_VEIL", 1, "mage, dragon", true);
        lost("skill_page_detection_of_vital_points");
        page("skill_page_lucky_strike", "LUCKY_STRIKE", 1, "merchant, assassin, archer, dragon", true);
        page("skill_page_treasure_hunter", "TREASURE_HUNTER", 1, "merchant, dragon", true);
        page("skill_page_good_omen", "GOOD_OMEN", 1, "merchant, dragon", true);
        page("skill_page_miracle_dodge", "MIRACLE_DODGE", 1, "assassin, archer, dragon", true);
        page("skill_page_fortune_coins", "TREASURE_HUNTER", 2, "merchant, dragon", false);
        page("skill_page_tireless_breath", "TIRELESS_BREATH", 1, "warrior, assassin, archer, dragon", true);
        page("skill_page_thick_skin", "STURDY_BODY", 1, "warrior, dragon", false);
        page("skill_page_solid_stance", "SOLID_STANCE", 1, "warrior, dragon", true);
        page("skill_page_combat_vigor", "COMBAT_VIGOR", 1, "warrior, assassin, archer, dragon", true);
        page("skill_page_second_wind", "SECOND_WIND", 1, "warrior, assassin, archer, dragon", true);
        lost("skill_page_mana_heart");
        page("skill_page_mana_font", "MANA_FONT", 1, "mage, dragon", true);
        page("skill_page_cellular_regeneration", "CELLULAR_REGENERATION", 1, "warrior, mage, dragon", true);
        page("skill_page_active_breathing", "COMBAT_VIGOR", 1, "warrior, assassin, archer, dragon", false);
        page("skill_page_spiritual_siphon", "SPIRITUAL_SIPHON", 1, "mage, dragon", true);
        page("skill_page_natural_recovery", "NATURAL_RECOVERY", 1, "all", true);
        page("skill_page_tenacity", "TENACITY", 1, "warrior, dragon", true);
        page("skill_page_adaptive_shield", "ADAPTIVE_SHIELD", 1, "warrior, dragon", true);
        page("skill_page_pain_tolerance", "IRON_RESOLVE", 1, "warrior, dragon", false);
        page("skill_page_hardening", "STONE_SKIN", 1, "warrior, dragon", false);
        page("skill_page_steel_nerves", "STEEL_NERVES", 1, "warrior, dragon", true);
        page("skill_page_robust_constitution", "ROBUST_CONSTITUTION", 1, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_life_force", "LIFE_FORCE", 1, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_vital_blood", "VITAL_BLOOD", 1, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_heart_of_oak", "HEART_OF_OAK", 1, "warrior, dragon", true);
        page("skill_page_perseverance", "PERSEVERANCE", 1, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_sharp_blade", "DEEP_SLASH", 2, "warrior, assassin, dragon", true);
        lost("skill_page_combatant_swiftness");
        page("skill_page_vital_pressure", "PRESSURE_POINT", 2, "assassin, archer, dragon", true);
        page("skill_page_relentless_hunt", "HAUNTING_THRUST", 2, "warrior, assassin, dragon", true);
        page("skill_page_steel_resolve", "IRON_RESOLVE", 2, "warrior, dragon", true);
        page("skill_page_expert_parry", "MINOR_PARRY", 2, "warrior, dragon", true);
        page("skill_page_beast_guard", "HUNTER_GUARD", 2, "warrior, archer, assassin, dragon", true);
        page("skill_page_iron_body", "STURDY_BODY", 2, "warrior, dragon", true);
        page("skill_page_gale_step", "WIND_STEP", 2, "novice, warrior, assassin, archer, dragon", true);
        lost("skill_page_thunder_reflexes");
        page("skill_page_catlike_poise", "ELDANIOR_SUPPLENESS", 2, "novice, assassin, archer, dragon", true);
        page("skill_page_razor_senses", "LUCKY_STRIKE", 2, "assassin, archer, dragon", false);
        page("skill_page_marathon_runner", "WIND_STEP", 3, "novice, warrior, assassin, archer, dragon", false);
        lost("skill_page_hawk_eye");
        page("skill_page_danger_sense", "SURVIVAL_INSTINCT", 2, "warrior, assassin, archer, dragon", true);
        page("skill_page_dark_vision", "NIGHT_VISION", 1, "assassin, archer, dragon", false);
        lost("skill_page_psychic_awareness");
        lost("skill_page_master_tracker");
        page("skill_page_lungs_of_steel", "TIRELESS_BREATH", 2, "warrior, assassin, archer, dragon", true);
        page("skill_page_armored_skin", "STURDY_BODY", 2, "warrior, dragon", false);
        page("skill_page_unmovable_mountain", "SOLID_STANCE", 2, "warrior, dragon", true);
        page("skill_page_battle_frenzy", "COMBAT_VIGOR", 2, "warrior, assassin, archer, dragon", true);
        page("skill_page_survivor_spirit", "SECOND_WIND", 2, "warrior, assassin, archer, dragon", true);
        page("skill_page_expanded_mind", "AWAKENED_MIND", 2, "mage, dragon", true);
        page("skill_page_spellblade", "ARCANE_STRIKE", 2, "mage, dragon", true);
        page("skill_page_arcane_shield", "MANA_BARRIER", 2, "mage, dragon", true);
        page("skill_page_unleashed_magic", "OVERFLOWING_POWER", 2, "mage, dragon", true);
        page("skill_page_astral_cloak", "MYSTIC_VEIL", 2, "mage, dragon", true);
        page("skill_page_critical_luck", "LUCKY_STRIKE", 2, "merchant, assassin, archer, dragon", true);
        page("skill_page_relic_hunter", "TREASURE_HUNTER", 2, "merchant, dragon", true);
        page("skill_page_fated_omen", "GOOD_OMEN", 2, "merchant, dragon", true);
        page("skill_page_phantom_dodge", "MIRACLE_DODGE", 2, "assassin, archer, dragon", true);
        page("skill_page_golden_touch", "TREASURE_HUNTER", 4, "merchant, dragon", false);
        page("skill_page_troll_blood", "CELLULAR_REGENERATION", 2, "warrior, mage, dragon", true);
        page("skill_page_iron_lungs", "COMBAT_VIGOR", 2, "warrior, assassin, archer, dragon", false);
        page("skill_page_soul_stealer", "SPIRITUAL_SIPHON", 2, "mage, dragon", true);
        page("skill_page_mana_stream", "MANA_FONT", 2, "mage, dragon", true);
        page("skill_page_vital_recovery", "NATURAL_RECOVERY", 2, "all", true);
        page("skill_page_unyielding", "TENACITY", 2, "warrior, dragon", true);
        page("skill_page_reactive_bulwark", "ADAPTIVE_SHIELD", 2, "warrior, dragon", true);
        page("skill_page_battle_scars", "IRON_RESOLVE", 2, "warrior, dragon", false);
        page("skill_page_fortification", "STONE_SKIN", 2, "warrior, dragon", false);
        page("skill_page_iron_will", "STEEL_NERVES", 2, "warrior, dragon", true);
        page("skill_page_steel_constitution", "ROBUST_CONSTITUTION", 2, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_overflowing_life", "LIFE_FORCE", 2, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_enriched_blood", "VITAL_BLOOD", 2, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_heart_of_iron", "HEART_OF_OAK", 2, "warrior, dragon", true);
        page("skill_page_undying", "PERSEVERANCE", 2, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_axe_mastery", "AXE_MASTERY", 2, "warrior, dragon", true);
        page("skill_page_bow_mastery", "BOW_MASTERY", 2, "archer, dragon", true);
        page("skill_page_spear_mastery", "SPEAR_MASTERY", 2, "warrior, dragon", true);
        page("skill_page_dagger_mastery", "DAGGER_MASTERY", 2, "assassin, archer, dragon", true);
        page("skill_page_minor_sword_mastery", "SWORD_MASTERY", 2, "warrior, assassin, dragon", true);
        page("skill_page_crimson_blade", "DEEP_SLASH", 3, "warrior, assassin, dragon", true);
        lost("skill_page_warrior_swiftness");
        page("skill_page_crushing_pressure", "PRESSURE_POINT", 3, "assassin, archer, dragon", true);
        page("skill_page_blood_hunt", "HAUNTING_THRUST", 3, "warrior, assassin, dragon", true);
        page("skill_page_titan_resolve", "IRON_RESOLVE", 3, "warrior, dragon", true);
        page("skill_page_master_parry", "MINOR_PARRY", 3, "warrior, dragon", true);
        page("skill_page_monster_slayer_guard", "HUNTER_GUARD", 3, "warrior, archer, assassin, dragon", true);
        page("skill_page_steel_body", "STURDY_BODY", 3, "warrior, dragon", true);
        page("skill_page_storm_step", "WIND_STEP", 3, "novice, warrior, assassin, archer, dragon", true);
        lost("skill_page_lightning_reflexes");
        page("skill_page_acrobatic_poise", "ELDANIOR_SUPPLENESS", 3, "novice, assassin, archer, dragon", true);
        page("skill_page_deadly_precision", "LUCKY_STRIKE", 3, "assassin, archer, dragon", false);
        lost("skill_page_eagle_vision");
        page("skill_page_combat_intuition", "SURVIVAL_INSTINCT", 3, "warrior, assassin, archer, dragon", true);
        page("skill_page_abyssal_vision", "NIGHT_VISION", 1, "assassin, archer, dragon", false);
        lost("skill_page_mind_reader");
        page("skill_page_dragon_lungs", "TIRELESS_BREATH", 3, "warrior, assassin, archer, dragon", true);
        page("skill_page_fortified_skin", "STURDY_BODY", 3, "warrior, dragon", false);
        page("skill_page_living_fortress", "SOLID_STANCE", 3, "warrior, dragon", true);
        page("skill_page_war_frenzy", "COMBAT_VIGOR", 3, "warrior, assassin, archer, dragon", true);
        page("skill_page_brilliant_mind", "AWAKENED_MIND", 3, "mage, dragon", true);
        page("skill_page_arcane_devastation", "ARCANE_STRIKE", 3, "mage, dragon", true);
        page("skill_page_mana_fortress", "MANA_BARRIER", 3, "mage, dragon", true);
        page("skill_page_pure_magic", "OVERFLOWING_POWER", 3, "mage, dragon", true);
        page("skill_page_destiny_strike", "LUCKY_STRIKE", 3, "merchant, assassin, archer, dragon", true);
        page("skill_page_artifact_hunter", "TREASURE_HUNTER", 3, "merchant, dragon", true);
        page("skill_page_prophecy_omen", "GOOD_OMEN", 3, "merchant, dragon", true);
        page("skill_page_shadow_dodge", "MIRACLE_DODGE", 3, "assassin, archer, dragon", true);
        page("skill_page_hydra_blood", "CELLULAR_REGENERATION", 3, "warrior, mage, dragon", true);
        page("skill_page_adamantine_lungs", "COMBAT_VIGOR", 3, "warrior, assassin, archer, dragon", false);
        page("skill_page_spirit_drain", "SPIRITUAL_SIPHON", 3, "mage, dragon", true);
        page("skill_page_mana_river", "MANA_FONT", 3, "mage, dragon", true);
        page("skill_page_unbreakable", "TENACITY", 3, "warrior, dragon", true);
        page("skill_page_adamantine_bulwark", "ADAPTIVE_SHIELD", 3, "warrior, dragon", true);
        page("skill_page_war_veteran", "IRON_RESOLVE", 3, "warrior, dragon", false);
        page("skill_page_iron_fortification", "STONE_SKIN", 3, "warrior, dragon", false);
        page("skill_page_titan_constitution", "ROBUST_CONSTITUTION", 3, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_bursting_life", "LIFE_FORCE", 3, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_ancient_blood", "VITAL_BLOOD", 3, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_heart_of_steel", "HEART_OF_OAK", 3, "warrior, dragon", true);
        page("skill_page_great_axe_mastery", "AXE_MASTERY", 3, "warrior, dragon", true);
        page("skill_page_marksman_mastery", "BOW_MASTERY", 3, "archer, dragon", true);
        page("skill_page_halberd_mastery", "SPEAR_MASTERY", 3, "warrior, dragon", true);
        page("skill_page_shadow_blade_mastery", "DAGGER_MASTERY", 3, "assassin, archer, dragon", true);
        page("skill_page_void_blade", "DEEP_SLASH", 4, "warrior, assassin, dragon", true);
        lost("skill_page_berserker_swiftness");
        page("skill_page_annihilating_pressure", "PRESSURE_POINT", 4, "assassin, archer, dragon", true);
        page("skill_page_death_hunt", "HAUNTING_THRUST", 4, "warrior, assassin, dragon", true);
        page("skill_page_god_resolve", "IRON_RESOLVE", 4, "warrior, dragon", true);
        page("skill_page_perfect_parry", "MINOR_PARRY", 4, "warrior, dragon", true);
        page("skill_page_dragon_slayer_guard", "HUNTER_GUARD", 4, "warrior, archer, assassin, dragon", true);
        page("skill_page_diamond_body", "STURDY_BODY", 4, "warrior, dragon", true);
        page("skill_page_void_step", "WIND_STEP", 4, "novice, warrior, assassin, archer, dragon", true);
        lost("skill_page_divine_reflexes");
        page("skill_page_gravity_defiance", "ELDANIOR_SUPPLENESS", 4, "novice, assassin, archer, dragon", true);
        page("skill_page_fatal_precision", "LUCKY_STRIKE", 4, "assassin, archer, dragon", false);
        lost("skill_page_omniscient_vision");
        page("skill_page_war_prophecy", "SURVIVAL_INSTINCT", 4, "warrior, assassin, archer, dragon", true);
        page("skill_page_void_sight", "NIGHT_VISION", 1, "assassin, archer, dragon", false);
        lost("skill_page_soul_reader");
        page("skill_page_titan_lungs", "TIRELESS_BREATH", 4, "warrior, assassin, archer, dragon", true);
        page("skill_page_adamantine_skin", "STURDY_BODY", 4, "warrior, dragon", false);
        page("skill_page_eternal_fortress", "SOLID_STANCE", 4, "warrior, dragon", true);
        page("skill_page_blood_frenzy", "COMBAT_VIGOR", 4, "warrior, assassin, archer, dragon", true);
        page("skill_page_genius_mind", "AWAKENED_MIND", 4, "mage, dragon", true);
        page("skill_page_arcane_annihilation", "ARCANE_STRIKE", 4, "mage, dragon", true);
        page("skill_page_mana_citadel", "MANA_BARRIER", 4, "mage, dragon", true);
        page("skill_page_arcane_supremacy", "OVERFLOWING_POWER", 4, "mage, dragon", true);
        page("skill_page_divine_strike", "LUCKY_STRIKE", 4, "merchant, assassin, archer, dragon", true);
        page("skill_page_legend_hunter", "TREASURE_HUNTER", 4, "merchant, dragon", true);
        page("skill_page_cosmic_omen", "GOOD_OMEN", 4, "merchant, dragon", true);
        page("skill_page_dimensional_dodge", "MIRACLE_DODGE", 4, "assassin, archer, dragon", true);
        page("skill_page_phoenix_blood", "CELLULAR_REGENERATION", 4, "warrior, mage, dragon", true);
        page("skill_page_mythril_lungs", "COMBAT_VIGOR", 4, "assassin, dragon", false);
        page("skill_page_arcane_vampirism", "SPIRITUAL_SIPHON", 4, "mage, dragon", true);
        page("skill_page_mana_ocean", "MANA_FONT", 4, "mage, dragon", true);
        page("skill_page_invincible", "TENACITY", 4, "warrior, dragon", true);
        page("skill_page_divine_bulwark", "ADAPTIVE_SHIELD", 4, "warrior, dragon", true);
        page("skill_page_war_legend", "IRON_RESOLVE", 4, "warrior, dragon", false);
        page("skill_page_mythril_fortification", "STONE_SKIN", 4, "warrior, dragon", false);
        page("skill_page_god_constitution", "ROBUST_CONSTITUTION", 4, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_eternal_life", "LIFE_FORCE", 4, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_dragon_blood", "VITAL_BLOOD", 4, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_heart_of_diamond", "HEART_OF_OAK", 4, "warrior, dragon", true);
        page("skill_page_war_axe_mastery", "AXE_MASTERY", 4, "warrior, dragon", true);
        page("skill_page_sniper_mastery", "BOW_MASTERY", 4, "archer, dragon", true);
        page("skill_page_dragon_spear_mastery", "SPEAR_MASTERY", 4, "warrior, dragon", true);
        page("skill_page_assassin_blade_mastery", "DAGGER_MASTERY", 4, "assassin, archer, dragon", true);
        page("skill_page_abyss_blade", "DEEP_SLASH", 5, "warrior, assassin, dragon", true);
        lost("skill_page_demigod_swiftness");
        page("skill_page_soul_crushing_pressure", "PRESSURE_POINT", 5, "assassin, archer, dragon", true);
        page("skill_page_immortal_resolve", "IRON_RESOLVE", 5, "warrior, dragon", true);
        page("skill_page_divine_parry", "MINOR_PARRY", 5, "warrior, dragon", true);
        page("skill_page_cosmic_body", "STURDY_BODY", 5, "warrior, dragon", true);
        page("skill_page_dimensional_step", "WIND_STEP", 5, "novice, warrior, assassin, archer, dragon", true);
        lost("skill_page_cosmic_reflexes");
        page("skill_page_absolute_precision", "LUCKY_STRIKE", 5, "assassin, archer, dragon", false);
        lost("skill_page_all_seeing_eye");
        page("skill_page_fate_vision", "SURVIVAL_INSTINCT", 5, "warrior, assassin, archer, dragon", true);
        lost("skill_page_true_sight");
        page("skill_page_cosmic_lungs", "TIRELESS_BREATH", 5, "warrior, assassin, archer, dragon", true);
        page("skill_page_divine_fortress", "SOLID_STANCE", 5, "warrior, dragon", true);
        page("skill_page_rage_frenzy", "COMBAT_VIGOR", 5, "warrior, assassin, archer, dragon", true);
        page("skill_page_cosmic_mind", "AWAKENED_MIND", 5, "mage, dragon", true);
        page("skill_page_arcane_oblivion", "ARCANE_STRIKE", 5, "mage, dragon", true);
        page("skill_page_absolute_supremacy", "OVERFLOWING_POWER", 5, "mage, dragon", true);
        page("skill_page_cosmic_strike", "LUCKY_STRIKE", 5, "merchant, assassin, archer, dragon", true);
        page("skill_page_myth_hunter", "TREASURE_HUNTER", 5, "merchant, dragon", true);
        page("skill_page_reality_dodge", "MIRACLE_DODGE", 5, "assassin, archer, dragon", true);
        page("skill_page_eternal_blood", "CELLULAR_REGENERATION", 5, "warrior, mage, dragon", true);
        page("skill_page_cosmic_vampirism", "SPIRITUAL_SIPHON", 5, "mage, dragon", true);
        page("skill_page_mana_infinity", "MANA_FONT", 5, "mage, dragon", true);
        page("skill_page_immortal_absolute", "TENACITY", 5, "warrior, dragon", true);
        page("skill_page_cosmic_bulwark", "ADAPTIVE_SHIELD", 5, "warrior, dragon", true);
        page("skill_page_eternal_fortification", "STONE_SKIN", 5, "warrior, dragon", false);
        page("skill_page_cosmic_constitution", "ROBUST_CONSTITUTION", 5, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_infinite_life", "LIFE_FORCE", 5, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_heart_of_eternity", "HEART_OF_OAK", 5, "warrior, dragon", true);
        page("skill_page_legendary_axe_mastery", "AXE_MASTERY", 5, "warrior, dragon", true);
        page("skill_page_legendary_sniper_mastery", "BOW_MASTERY", 5, "archer, dragon", true);
        page("skill_page_legendary_dagger_mastery", "DAGGER_MASTERY", 5, "assassin, archer, dragon", true);
        page("skill_page_genesis_edge", "DEEP_SLASH", 6, "warrior, assassin, dragon", true);
        lost("skill_page_god_slayer_swiftness");
        page("skill_page_eternity_resolve", "IRON_RESOLVE", 6, "warrior, dragon", true);
        page("skill_page_celestial_parry", "MINOR_PARRY", 6, "warrior, dragon", true);
        page("skill_page_celestial_step", "WIND_STEP", 6, "novice, warrior, assassin, archer, dragon", true);
        page("skill_page_omniscient_precision", "LUCKY_STRIKE", 6, "assassin, archer, dragon", false);
        lost("skill_page_genesis_vision");
        lost("skill_page_omni_sight");
        page("skill_page_celestial_lungs", "TIRELESS_BREATH", 6, "warrior, assassin, archer, dragon", true);
        page("skill_page_celestial_fortress", "SOLID_STANCE", 6, "warrior, dragon", true);
        page("skill_page_infinite_mind", "AWAKENED_MIND", 6, "mage, dragon", true);
        page("skill_page_arcane_genesis", "ARCANE_STRIKE", 6, "mage, dragon", true);
        page("skill_page_genesis_strike", "LUCKY_STRIKE", 6, "merchant, assassin, archer, dragon", true);
        page("skill_page_time_dodge", "MIRACLE_DODGE", 6, "assassin, archer, dragon", true);
        page("skill_page_genesis_blood", "CELLULAR_REGENERATION", 6, "warrior, mage, dragon", true);
        page("skill_page_infinite_vampirism", "SPIRITUAL_SIPHON", 6, "mage, dragon", true);
        page("skill_page_celestial_immortality", "TENACITY", 6, "warrior, dragon", true);
        page("skill_page_genesis_bulwark", "ADAPTIVE_SHIELD", 6, "warrior, dragon", true);
        page("skill_page_celestial_constitution", "ROBUST_CONSTITUTION", 6, "warrior, mage, assassin, archer, merchant, dragon", true);
        page("skill_page_heart_of_genesis", "HEART_OF_OAK", 6, "warrior, dragon", true);
        page("skill_page_divin_axe_mastery", "AXE_MASTERY", 6, "warrior, dragon", true);
        page("skill_page_divin_dagger_mastery", "DAGGER_MASTERY", 6, "assassin, archer, dragon", true);
        page("skill_page_creator_edge", "DEEP_SLASH", 6, "warrior, assassin, dragon", false);
        lost("skill_page_creator_swiftness");
        page("skill_page_absolute_resolve", "IRON_RESOLVE", 6, "warrior, dragon", false);
        page("skill_page_creator_parry", "MINOR_PARRY", 6, "warrior, dragon", false);
        page("skill_page_creator_step", "WIND_STEP", 6, "novice, warrior, assassin, archer, dragon", false);
        page("skill_page_creator_precision", "LUCKY_STRIKE", 6, "assassin, archer, dragon", false);
        lost("skill_page_creator_vision");
        lost("skill_page_absolute_sight");
        page("skill_page_creator_lungs", "TIRELESS_BREATH", 6, "warrior, assassin, archer, dragon", false);
        page("skill_page_creator_fortress", "SOLID_STANCE", 6, "warrior, dragon", false);
        page("skill_page_creator_mind", "AWAKENED_MIND", 6, "mage, dragon", false);
        page("skill_page_arcane_creation", "ARCANE_STRIKE", 6, "mage, dragon", false);
        page("skill_page_creator_strike", "LUCKY_STRIKE", 6, "merchant, assassin, archer, dragon", false);
        page("skill_page_fate_dodge", "MIRACLE_DODGE", 6, "assassin, archer, dragon", false);
        page("skill_page_creator_blood", "CELLULAR_REGENERATION", 6, "warrior, mage, dragon", false);
        page("skill_page_creator_vampirism", "SPIRITUAL_SIPHON", 6, "mage, dragon", false);
        page("skill_page_true_immortality", "TENACITY", 6, "warrior, dragon", false);
        page("skill_page_creator_bulwark", "ADAPTIVE_SHIELD", 6, "warrior, dragon", false);
        page("skill_page_creator_constitution", "ROBUST_CONSTITUTION", 6, "warrior, mage, assassin, archer, merchant, dragon", false);
        page("skill_page_heart_of_creation", "HEART_OF_OAK", 6, "warrior, dragon", false);
        page("skill_page_creator_axe_mastery", "AXE_MASTERY", 6, "warrior, dragon", false);
        page("skill_page_creator_dagger_mastery", "DAGGER_MASTERY", 6, "assassin, archer, dragon", false);
        lost("skill_page_baton_magique");
        page("skill_page_peche_orgueil", "PECHE_ORGUEIL", 7, "all", true);
        page("skill_page_peche_colere", "PECHE_COLERE", 7, "all", true);
        page("skill_page_peche_avarice", "PECHE_AVARICE", 7, "all", true);
        page("skill_page_peche_envie", "PECHE_ENVIE", 7, "all", true);
        page("skill_page_peche_luxure", "PECHE_LUXURE", 7, "all", true);
        page("skill_page_peche_gourmandise", "PECHE_GOURMANDISE", 7, "all", true);
        page("skill_page_peche_paresse", "PECHE_PARESSE", 7, "all", true);
        page("skill_page_ange_humilite", "ANGE_HUMILITE", 7, "all", true);
        page("skill_page_ange_patience", "ANGE_PATIENCE", 7, "all", true);
        page("skill_page_ange_temperance", "ANGE_TEMPERANCE", 7, "all", true);
        page("skill_page_ange_charite", "ANGE_CHARITE", 7, "all", true);
        page("skill_page_ange_bienveillance", "ANGE_BIENVEILLANCE", 7, "all", true);
        page("skill_page_ange_chastete", "ANGE_CHASTETE", 7, "all", true);
        page("skill_page_ange_diligence", "ANGE_DILIGENCE", 7, "all", true);
        metier("Eldanior_Parchemin_Metier_Artisanat", "ARTISANAT");
        metier("skill_page_artisanat", "ARTISANAT");
        metier("Eldanior_Parchemin_Metier_Artisan", "CRAFT_ARTISAN_BASE");
        metier("Eldanior_Parchemin_Metier_Cuisine", "CRAFT_CUISINE");
        metier("skill_page_craft_cuisine", "CRAFT_CUISINE");
        metier("Eldanior_Parchemin_Metier_Fonderie", "CRAFT_FONDERIE");
        metier("skill_page_craft_fonderie", "CRAFT_FONDERIE");
        metier("Eldanior_Parchemin_Metier_Armurerie", "CRAFT_ARMURERIE");
        metier("skill_page_craft_armurerie", "CRAFT_ARMURERIE");
        metier("Eldanior_Parchemin_Metier_Forge_Armes", "CRAFT_FORGE_ARMES");
        metier("skill_page_craft_forge_armes", "CRAFT_FORGE_ARMES");
        metier("Eldanior_Parchemin_Metier_Forge_Divine", "CRAFT_FORGE_DIVINE");
        metier("skill_page_craft_forge_divine", "CRAFT_FORGE_DIVINE");
        metier("Eldanior_Parchemin_Metier_Tannerie", "CRAFT_TANNERIE");
        metier("skill_page_craft_tannerie", "CRAFT_TANNERIE");
        metier("Eldanior_Parchemin_Metier_Alchimie", "CRAFT_ALCHIMIE");
        metier("skill_page_craft_alchimie", "CRAFT_ALCHIMIE");
        metier("Eldanior_Parchemin_Metier_Scierie", "CRAFT_SCIERIE");
        metier("skill_page_craft_scierie", "CRAFT_SCIERIE");
        metier("Eldanior_Parchemin_Metier_Agriculture", "CRAFT_AGRICULTURE");
        metier("skill_page_craft_agriculture", "CRAFT_AGRICULTURE");
        metier("Eldanior_Parchemin_Metier_Recyclage", "CRAFT_RECYCLAGE");
        metier("skill_page_craft_recyclage", "CRAFT_RECYCLAGE");
    }

    private static void page(String item, String skillId, int rank, String classes, boolean main) {
        PAGES.put(item, new Page(item, skillId, rank, classes, main));
    }

    private static void metier(String item, String metierId) { METIERS.put(item, metierId); }

    private static void lost(String item) { LOST.add(item); }

    public static Optional<Page> getPage(String itemId) { return Optional.ofNullable(PAGES.get(itemId)); }

    /** Metier appris par ce parchemin (ou ancienne page de metier). */
    public static Optional<String> getMetier(String itemId) { return Optional.ofNullable(METIERS.get(itemId)); }

    /** Page d'un passif supprime le 2026-11-24 : elle ne s'apprend plus. */
    public static boolean isLost(String itemId) { return LOST.contains(itemId); }

    public static Collection<Page> getAllPages() { return Collections.unmodifiableCollection(PAGES.values()); }

    /** Page de reference d'un passif a un rang (null s'il n'y en a pas). */
    public static String pageFor(String skillId, int rank) {
        for (Page p : PAGES.values()) if (p.main() && p.skillId().equals(skillId) && p.rank() == rank) return p.itemId();
        return null;
    }

    /** Parchemin d'un metier. */
    public static String scrollFor(String metierId) {
        com.eldanior.system.skills.skillsInteraction.Metier m = com.eldanior.system.skills.skillsInteraction.Metier.fromName(metierId);
        return m != null ? m.getScrollItem() : null;
    }
}
