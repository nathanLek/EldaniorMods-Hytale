package com.eldanior.system.skills;

import com.eldanior.system.skills.models.SkillModel;

import java.util.*;

public class SkillManager {

    private static final Map<String, SkillModel> ITEM_TO_SKILL = new HashMap<>();
    private static final Map<String, SkillModel> ID_TO_SKILL = new HashMap<>();

    public static void init() {

        // --- ATTAQUE ---
        register("skill_page_mauvais_presage", new SkillModel(
                "MAUVAIS_PRESAGE", null, "Mauvais Présage", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_koda_judgment", new SkillModel(
                "KODA_JUDGMENT", null, "Jugement de Koda", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), // Légendaire, donc pas d'évolution suivante pour le moment !
                List.of()
        ));
        register("skill_page_opportunist_strike", new SkillModel(
                "OPPORTUNIST_STRIKE", null, "Frappe Opportuniste", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_haunting_thrust", new SkillModel(
                "HAUNTING_THRUST", null, "Estocade Obsédante", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("RELENTLESS_HUNT", "BLOOD_HUNT", "DEATH_HUNT"),
                List.of()
        ));
        register("skill_page_pressure_point", new SkillModel(
                "PRESSURE_POINT", null, "Point de Pression", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("VITAL_PRESSURE", "CRUSHING_PRESSURE", "ANNIHILATING_PRESSURE", "SOUL_CRUSHING_PRESSURE"),
                List.of()
        ));
        register("skill_page_duelist_swiftness", new SkillModel(
                "DUELIST_SWIFTNESS", null, "Vivacité du Duelliste", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COMBATANT_SWIFTNESS", "WARRIOR_SWIFTNESS", "BERSERKER_SWIFTNESS", "DEMIGOD_SWIFTNESS", "GOD_SLAYER_SWIFTNESS", "CREATOR_SWIFTNESS"),
                List.of()
        ));
        register("skill_page_deep_slash", new SkillModel(
                "DEEP_SLASH", null, "Entaille Profonde", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SHARP_BLADE", "CRIMSON_BLADE", "VOID_BLADE", "ABYSS_BLADE", "GENESIS_EDGE", "CREATOR_EDGE"),
                List.of()
        ));

        register("skill_page_instinctive_strike", new SkillModel(
                "INSTINCTIVE_STRIKE", null, "Frappe Instinctive", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PREDATORY_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of()
        ));
        register("skill_page_predatory_strike", new SkillModel(
                "PREDATORY_STRIKE",
                null, // Ici, on met l'ID du skill précédent si c'est une évolution
                "Frappe de Prédateur",
                "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FURY_STRIKE","SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("INSTINCTIVE_STRIKE")
        ));
        register("skill_page_fury_strike", new SkillModel(
                "FURY_STRIKE",
                "null", // Ici, on met l'ID du skill précédent si c'est une évolution
                "Frappe de Fureur",
                "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("INSTINCTIVE_STRIKE", "PREDATORY_STRIKE")
        ));
        register("skill_page_seismic_strike", new SkillModel(
                "SEISMIC_STRIKE",
                null,
                "Frappe Sismique", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("INSTINCTIVE_STRIKE", "PREDATORY_STRIKE", "FURY_STRIKE")
        ));
        register("skill_page_phantom_strike", new SkillModel(
                "PHANTOM_STRIKE",
                null,
                "Frappe Fantôme", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("INSTINCTIVE_STRIKE", "PREDATORY_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE")
        ));
        register("skill_page_annihilator_strike", new SkillModel(
                "ANNIHILATOR_STRIKE",
                null,
                "Frappe de l'Annihilateur", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("JUDGMENT_OF_GENESIS"),
                List.of("INSTINCTIVE_STRIKE", "PREDATORY_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE")
        ));
        register("skill_page_judgment_of_genesis", new SkillModel(
                "JUDGMENT_OF_GENESIS",
                null,
                "Décret de la Genèse", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("INSTINCTIVE_STRIKE", "PREDATORY_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE")
        ));

        // --- ACTIVE : TRANSFORMATION ---
        register("skill_page_morph_dragon", new SkillModel(
                "MORPH_DRAGON", "Morph_Of_The_Ancient_Dragon", "Forme du Dragon Ancien", "dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));

        // --- DEFENSE ---
        register("skill_page_dyna_aegis", new SkillModel(
                "DYNA_AEGIS", null, "Égide de Dyna", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_iron_resolve", new SkillModel(
                "IRON_RESOLVE", null, "Résolution de Fer", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STEEL_RESOLVE", "TITAN_RESOLVE", "GOD_RESOLVE", "IMMORTAL_RESOLVE", "ETERNITY_RESOLVE", "ABSOLUTE_RESOLVE"),
                List.of()
        ));
        register("skill_page_minor_parry", new SkillModel(
                "MINOR_PARRY", null, "Parade Mineure", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("EXPERT_PARRY", "MASTER_PARRY", "PERFECT_PARRY", "DIVINE_PARRY", "CELESTIAL_PARRY", "CREATOR_PARRY"),
                List.of()
        ));
        register("skill_page_hunter_guard", new SkillModel(
                "HUNTER_GUARD", null, "Garde de Chasseur", "warrior, archer, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BEAST_GUARD", "MONSTER_SLAYER_GUARD", "DRAGON_SLAYER_GUARD"),
                List.of()
        ));
        register("skill_page_sturdy_body", new SkillModel(
                "STURDY_BODY", null, "Corps Robuste", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_BODY", "STEEL_BODY", "DIAMOND_BODY", "COSMIC_BODY"),
                List.of()
        ));
        register("skill_page_stone_skin", new SkillModel(
                "STONE_SKIN", null, "Peau de Pierre", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of() // Puissance minimal !
        ));
        register("skill_page_bronze_skin", new SkillModel(
                "BRONZE_SKIN", null, "Peau de Bronze", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN")
        ));
        register("skill_page_iron_skin", new SkillModel(
                "IRON_SKIN", null, "Peau de Fer", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN")
        ));
        register("skill_page_steel_skin", new SkillModel(
                "STEEL_SKIN", null, "Peau d'Acier", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN")
        ));
        register("skill_page_obsidian_skin", new SkillModel(
                "OBSIDIAN_SKIN", null, "Peau d'Obsidienne", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN")
        ));
        register("skill_page_diamond_skin", new SkillModel(
                "DIAMOND_SKIN", null, "Peau de Diamant", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN")
        ));
        register("skill_page_divine_aegis", new SkillModel(
                "DIVINE_AEGIS", null, "Égide Divine", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), // Puissance maximale atteinte !
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN")
        ));

        // --- AGILITY ---
        register("skill_page_wind_step", new SkillModel(
                "WIND_STEP", null, "Pas de Vent", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GALE_STEP", "STORM_STEP", "VOID_STEP", "DIMENSIONAL_STEP", "CELESTIAL_STEP", "CREATOR_STEP"),
                List.of()
        ));
        register("skill_page_light_reflexes", new SkillModel(
                "LIGHT_REFLEXES", null, "Réflexes Éclairs", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("THUNDER_REFLEXES", "LIGHTNING_REFLEXES", "DIVINE_REFLEXES", "COSMIC_REFLEXES"),
                List.of()
        ));
        register("skill_page_suppleness", new SkillModel(
                "ELDANIOR_SUPPLENESS", null, "Souplesse d'Eldanior", "novice, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CATLIKE_POISE", "ACROBATIC_POISE", "GRAVITY_DEFIANCE"),
                List.of()
        ));
        register("skill_page_athleticism", new SkillModel(
                "ATHLETICISM", null, "Athlétisme", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MARATHON_RUNNER"),
                List.of()
        ));
        register("skill_page_eagle_eye", new SkillModel(
                "EAGLE_EYE", null, "Œil de Rapace", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HAWK_EYE", "EAGLE_VISION", "OMNISCIENT_VISION", "ALL_SEEING_EYE", "GENESIS_VISION", "CREATOR_VISION"), List.of()
        ));

        register("skill_page_tono_squive", new SkillModel(
                "TONOSQUIVE", null, "Esquive de Tono", "assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        // --- DETECTION ---
        register("skill_page_survival_instinct", new SkillModel(
                "SURVIVAL_INSTINCT", null, "Instinct de Survie", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DANGER_SENSE", "COMBAT_INTUITION", "WAR_PROPHECY", "FATE_VISION"), List.of()
        ));
        register("skill_page_keen_senses", new SkillModel(
                "KEEN_SENSES", null, "Sens Aiguisés", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("RAZOR_SENSES", "DEADLY_PRECISION", "FATAL_PRECISION", "ABSOLUTE_PRECISION", "OMNISCIENT_PRECISION", "CREATOR_PRECISION"),
                List.of()
        ));
        register("skill_page_universal_detection", new SkillModel(
                "UNIVERSAL_DETECTION", null, "Detection Universel", "mage, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SURVIVAL_INSTINCT")
        ));

        register("skill_page_night_vision", new SkillModel(
                "NIGHT_VISION", null, "Vision Nocturne", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DARK_VISION", "ABYSSAL_VISION", "VOID_SIGHT"), List.of()
        ));

        register("skill_page_sixth_sense", new SkillModel(
                "SIXTH_SENSE", null, "Sixième Sens", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PSYCHIC_AWARENESS", "MIND_READER", "SOUL_READER", "TRUE_SIGHT", "OMNI_SIGHT", "ABSOLUTE_SIGHT"), List.of()
        ));

        register("skill_page_tracker", new SkillModel(
                "TRACKER", null, "Pisteur", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MASTER_TRACKER"), List.of()
        ));

        // --- MAITRISE ---
        register("skill_page_sword_mastery", new SkillModel(
                "SWORD_MASTERY", null, "Maîtrise de l'Épée", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("MINOR_SWORD_MASTERY")
        ));


        // --- MAGIE ---
        register("skill_page_fly", new SkillModel(
                "VOL", null, "Vol", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_mana_well", new SkillModel(
                "MANAWELL", null, "Maîtrise de l'Épée", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_awakened_mind", new SkillModel(
                "AWAKENED_MIND", null, "Esprit Éveillé", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("EXPANDED_MIND", "BRILLIANT_MIND", "GENIUS_MIND", "COSMIC_MIND", "INFINITE_MIND", "CREATOR_MIND"),
                List.of()
        ));

        register("skill_page_arcane_strike", new SkillModel(
                "ARCANE_STRIKE", null, "Frappe Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SPELLBLADE", "ARCANE_DEVASTATION", "ARCANE_ANNIHILATION", "ARCANE_OBLIVION", "ARCANE_GENESIS", "ARCANE_CREATION"),
                List.of()
        ));

        register("skill_page_mana_barrier", new SkillModel(
                "MANA_BARRIER", null, "Barrière de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_SHIELD", "MANA_FORTRESS", "MANA_CITADEL"),
                List.of()
        ));

        register("skill_page_overflowing_power", new SkillModel(
                "OVERFLOWING_POWER", null, "Puissance Débordante", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNLEASHED_MAGIC", "PURE_MAGIC", "ARCANE_SUPREMACY", "ABSOLUTE_SUPREMACY"),
                List.of()
        ));

        register("skill_page_mystic_veil", new SkillModel(
                "MYSTIC_VEIL", null, "Voile Mystique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ASTRAL_CLOAK"),
                List.of()
        ));

        // --- ARTISANAT / CRAFT ---
        register("skill_page_artisanat", new SkillModel(
                "ARTISANAT", null, "Artisanat", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_craft_cuisine", new SkillModel(
                "CRAFT_CUISINE", null, "Cuisine", "merchant, all",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_fonderie", new SkillModel(
                "CRAFT_FONDERIE", null, "Fonderie", "merchant, warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_armurerie", new SkillModel(
                "CRAFT_ARMURERIE", null, "Armurerie", "merchant, warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_forge_armes", new SkillModel(
                "CRAFT_FORGE_ARMES", null, "Forge d'Armes", "merchant, warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_tannerie", new SkillModel(
                "CRAFT_TANNERIE", null, "Tannerie", "merchant, archer",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_alchimie", new SkillModel(
                "CRAFT_ALCHIMIE", null, "Alchimie", "merchant, mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_scierie", new SkillModel(
                "CRAFT_SCIERIE", null, "Scierie", "merchant, all",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_agriculture", new SkillModel(
                "CRAFT_AGRICULTURE", null, "Agriculture", "merchant, all",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_craft_recyclage", new SkillModel(
                "CRAFT_RECYCLAGE", null, "Recyclage", "merchant, all",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, List.of(), List.of()
        ));
        register("skill_page_detection_of_vital_points", new SkillModel(
                "DETECTIONOFVITALPOINTS", null, "Maîtrise de l'Épée", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_lucky_strike", new SkillModel(
                "LUCKY_STRIKE", null, "Frappe Chanceuse", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CRITICAL_LUCK", "DESTINY_STRIKE", "DIVINE_STRIKE", "COSMIC_STRIKE", "GENESIS_STRIKE", "CREATOR_STRIKE"),
                List.of()
        ));
        register("skill_page_treasure_hunter", new SkillModel(
                "TREASURE_HUNTER", null, "Chasseur de Trésors", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("RELIC_HUNTER", "ARTIFACT_HUNTER", "LEGEND_HUNTER", "MYTH_HUNTER"),
                List.of()
        ));
        register("skill_page_good_omen", new SkillModel(
                "GOOD_OMEN", null, "Bon Présage", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FATED_OMEN", "PROPHECY_OMEN", "COSMIC_OMEN"),
                List.of()
        ));

        register("skill_page_miracle_dodge", new SkillModel(
                "MIRACLE_DODGE", null, "Esquive Miraculeuse", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PHANTOM_DODGE", "SHADOW_DODGE", "DIMENSIONAL_DODGE", "REALITY_DODGE", "TIME_DODGE", "FATE_DODGE"),
                List.of()
        ));
        register("skill_page_fortune_coins", new SkillModel(
                "FORTUNE_COINS", null, "Fortune Dorée", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GOLDEN_TOUCH"),
                List.of()
        ));

        // --- ENDURANCE ---
        register("skill_page_tireless_breath", new SkillModel(
                "TIRELESS_BREATH", null, "Souffle Inépuisable", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LUNGS_OF_STEEL", "DRAGON_LUNGS", "TITAN_LUNGS", "COSMIC_LUNGS", "CELESTIAL_LUNGS", "CREATOR_LUNGS"),
                List.of()
        ));

        register("skill_page_thick_skin", new SkillModel(
                "THICK_SKIN", null, "Peau Épaisse", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARMORED_SKIN", "FORTIFIED_SKIN", "ADAMANTINE_SKIN"),
                List.of()
        ));

        register("skill_page_solid_stance", new SkillModel(
                "SOLID_STANCE", null, "Posture Solide", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNMOVABLE_MOUNTAIN", "LIVING_FORTRESS", "ETERNAL_FORTRESS", "DIVINE_FORTRESS", "CELESTIAL_FORTRESS", "CREATOR_FORTRESS"),
                List.of()
        ));

        register("skill_page_combat_vigor", new SkillModel(
                "COMBAT_VIGOR", null, "Vigueur Combative", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BATTLE_FRENZY", "WAR_FRENZY", "BLOOD_FRENZY", "RAGE_FRENZY"),
                List.of()
        ));

        register("skill_page_second_wind", new SkillModel(
                "SECOND_WIND", null, "Second Souffle", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SURVIVOR_SPIRIT"),
                List.of()
        ));

        // --- REGENERATION ---
        register("skill_page_mana_heart", new SkillModel(
                "MANA_HEART", null, "Coeur de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_mana_font", new SkillModel(
                "MANA_FONT", null, "Source de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MANA_STREAM", "MANA_RIVER", "MANA_OCEAN", "MANA_INFINITY"),
                List.of()
        ));
        register("skill_page_cellular_regeneration", new SkillModel(
                "CELLULAR_REGENERATION", null, "Régénération Cellulaire", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TROLL_BLOOD", "HYDRA_BLOOD", "PHOENIX_BLOOD", "ETERNAL_BLOOD", "GENESIS_BLOOD", "CREATOR_BLOOD"),
                List.of()
        ));
        register("skill_page_active_breathing", new SkillModel(
                "ACTIVE_BREATHING", null, "Respiration Active", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_LUNGS", "ADAMANTINE_LUNGS", "MYTHRIL_LUNGS"),
                List.of()
        ));
        register("skill_page_spiritual_siphon", new SkillModel(
                "SPIRITUAL_SIPHON", null, "Siphon Spirituel", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SOUL_STEALER", "SPIRIT_DRAIN", "ARCANE_VAMPIRISM", "COSMIC_VAMPIRISM", "INFINITE_VAMPIRISM", "CREATOR_VAMPIRISM"),
                List.of()
        ));
        register("skill_page_natural_recovery", new SkillModel(
                "NATURAL_RECOVERY", null, "Rétablissement Naturel", "all",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("VITAL_RECOVERY"),
                List.of()
        ));

        // --- RÉSISTANCE ---
        register("skill_page_tenacity", new SkillModel(
                "TENACITY", null, "Ténacité", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNYIELDING", "UNBREAKABLE", "INVINCIBLE", "IMMORTAL_ABSOLUTE", "CELESTIAL_IMMORTALITY", "TRUE_IMMORTALITY"),
                List.of()
        ));
        register("skill_page_adaptive_shield", new SkillModel(
                "ADAPTIVE_SHIELD", null, "Bouclier Adaptatif", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("REACTIVE_BULWARK", "ADAMANTINE_BULWARK", "DIVINE_BULWARK", "COSMIC_BULWARK", "GENESIS_BULWARK", "CREATOR_BULWARK"),
                List.of()
        ));
        register("skill_page_pain_tolerance", new SkillModel(
                "PAIN_TOLERANCE", null, "Tolérance à la Douleur", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BATTLE_SCARS", "WAR_VETERAN", "WAR_LEGEND"),
                List.of()
        ));
        register("skill_page_hardening", new SkillModel(
                "HARDENING", null, "Endurcissement", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FORTIFICATION", "IRON_FORTIFICATION", "MYTHRIL_FORTIFICATION", "ETERNAL_FORTIFICATION"),
                List.of()
        ));
        register("skill_page_steel_nerves", new SkillModel(
                "STEEL_NERVES", null, "Nerfs d'Acier", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_WILL"),
                List.of()
        ));

        // --- VIE ---
        register("skill_page_robust_constitution", new SkillModel(
                "ROBUST_CONSTITUTION", null, "Constitution Robuste", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STEEL_CONSTITUTION", "TITAN_CONSTITUTION", "GOD_CONSTITUTION", "COSMIC_CONSTITUTION", "CELESTIAL_CONSTITUTION", "CREATOR_CONSTITUTION"),
                List.of()
        ));
        register("skill_page_life_force", new SkillModel(
                "LIFE_FORCE", null, "Force Vitale", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("OVERFLOWING_LIFE", "BURSTING_LIFE", "ETERNAL_LIFE", "INFINITE_LIFE"),
                List.of()
        ));
        register("skill_page_vital_blood", new SkillModel(
                "VITAL_BLOOD", null, "Sang Vital", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ENRICHED_BLOOD", "ANCIENT_BLOOD", "DRAGON_BLOOD"),
                List.of()
        ));
        register("skill_page_heart_of_oak", new SkillModel(
                "HEART_OF_OAK", null, "Coeur de Chêne", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HEART_OF_IRON", "HEART_OF_STEEL", "HEART_OF_DIAMOND", "HEART_OF_ETERNITY", "HEART_OF_GENESIS", "HEART_OF_CREATION"),
                List.of()
        ));
        register("skill_page_perseverance", new SkillModel(
                "PERSEVERANCE", null, "Persévérance", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNDYING"),
                List.of()
        ));

        // =============================================
        // === UNCOMMON SKILLS (Évolutions Peu Communes) ===
        // =============================================

        // --- ATTAQUE (Uncommon) ---
        register("skill_page_sharp_blade", new SkillModel(
                "SHARP_BLADE", null, "Lame Aiguisée", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CRIMSON_BLADE", "VOID_BLADE", "ABYSS_BLADE", "GENESIS_EDGE", "CREATOR_EDGE"),
                List.of("DEEP_SLASH")
        ));
        register("skill_page_combatant_swiftness", new SkillModel(
                "COMBATANT_SWIFTNESS", null, "Vivacité du Combattant", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("WARRIOR_SWIFTNESS", "BERSERKER_SWIFTNESS", "DEMIGOD_SWIFTNESS", "GOD_SLAYER_SWIFTNESS", "CREATOR_SWIFTNESS"),
                List.of("DUELIST_SWIFTNESS")
        ));
        register("skill_page_vital_pressure", new SkillModel(
                "VITAL_PRESSURE", null, "Pression Vitale", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CRUSHING_PRESSURE", "ANNIHILATING_PRESSURE", "SOUL_CRUSHING_PRESSURE"),
                List.of("PRESSURE_POINT")
        ));
        register("skill_page_relentless_hunt", new SkillModel(
                "RELENTLESS_HUNT", null, "Chasse Implacable", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BLOOD_HUNT", "DEATH_HUNT"),
                List.of("HAUNTING_THRUST")
        ));

        // --- DEFENSE (Uncommon) ---
        register("skill_page_steel_resolve", new SkillModel(
                "STEEL_RESOLVE", null, "Résolution d'Acier", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TITAN_RESOLVE", "GOD_RESOLVE", "IMMORTAL_RESOLVE", "ETERNITY_RESOLVE", "ABSOLUTE_RESOLVE"),
                List.of("IRON_RESOLVE")
        ));
        register("skill_page_expert_parry", new SkillModel(
                "EXPERT_PARRY", null, "Parade Experte", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MASTER_PARRY", "PERFECT_PARRY", "DIVINE_PARRY", "CELESTIAL_PARRY", "CREATOR_PARRY"),
                List.of("MINOR_PARRY")
        ));
        register("skill_page_beast_guard", new SkillModel(
                "BEAST_GUARD", null, "Garde Bestiale", "warrior, archer, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MONSTER_SLAYER_GUARD", "DRAGON_SLAYER_GUARD"),
                List.of("HUNTER_GUARD")
        ));
        register("skill_page_iron_body", new SkillModel(
                "IRON_BODY", null, "Corps de Fer", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STEEL_BODY", "DIAMOND_BODY", "COSMIC_BODY"),
                List.of("STURDY_BODY")
        ));

        // --- AGILITÉ (Uncommon) ---
        register("skill_page_gale_step", new SkillModel(
                "GALE_STEP", null, "Pas de Bourrasque", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STORM_STEP", "VOID_STEP", "DIMENSIONAL_STEP", "CELESTIAL_STEP", "CREATOR_STEP"),
                List.of("WIND_STEP")
        ));
        register("skill_page_thunder_reflexes", new SkillModel(
                "THUNDER_REFLEXES", null, "Réflexes Tonnerre", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LIGHTNING_REFLEXES", "DIVINE_REFLEXES", "COSMIC_REFLEXES"),
                List.of("LIGHT_REFLEXES")
        ));
        register("skill_page_catlike_poise", new SkillModel(
                "CATLIKE_POISE", null, "Grâce Féline", "novice, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ACROBATIC_POISE", "GRAVITY_DEFIANCE"),
                List.of("ELDANIOR_SUPPLENESS")
        ));
        register("skill_page_razor_senses", new SkillModel(
                "RAZOR_SENSES", null, "Sens Tranchants", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DEADLY_PRECISION", "FATAL_PRECISION", "ABSOLUTE_PRECISION", "OMNISCIENT_PRECISION", "CREATOR_PRECISION"),
                List.of("KEEN_SENSES")
        ));
        register("skill_page_marathon_runner", new SkillModel(
                "MARATHON_RUNNER", null, "Marathonien", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("ATHLETICISM")
        ));

        // --- DETECTION (Uncommon) ---
        register("skill_page_hawk_eye", new SkillModel(
                "HAWK_EYE", null, "Œil de Faucon", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("EAGLE_VISION", "OMNISCIENT_VISION", "ALL_SEEING_EYE", "GENESIS_VISION", "CREATOR_VISION"),
                List.of("EAGLE_EYE")
        ));
        register("skill_page_danger_sense", new SkillModel(
                "DANGER_SENSE", null, "Sens du Danger", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COMBAT_INTUITION", "WAR_PROPHECY", "FATE_VISION"),
                List.of("SURVIVAL_INSTINCT")
        ));
        register("skill_page_dark_vision", new SkillModel(
                "DARK_VISION", null, "Vision des Ténèbres", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ABYSSAL_VISION", "VOID_SIGHT"),
                List.of("NIGHT_VISION")
        ));
        register("skill_page_psychic_awareness", new SkillModel(
                "PSYCHIC_AWARENESS", null, "Conscience Psychique", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MIND_READER", "SOUL_READER", "TRUE_SIGHT", "OMNI_SIGHT", "ABSOLUTE_SIGHT"),
                List.of("SIXTH_SENSE")
        ));
        register("skill_page_master_tracker", new SkillModel(
                "MASTER_TRACKER", null, "Pisteur Maître", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("TRACKER")
        ));

        // --- ENDURANCE (Uncommon) ---
        register("skill_page_lungs_of_steel", new SkillModel(
                "LUNGS_OF_STEEL", null, "Poumons d'Acier", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DRAGON_LUNGS", "TITAN_LUNGS", "COSMIC_LUNGS", "CELESTIAL_LUNGS", "CREATOR_LUNGS"),
                List.of("TIRELESS_BREATH")
        ));
        register("skill_page_armored_skin", new SkillModel(
                "ARMORED_SKIN", null, "Peau Blindée", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FORTIFIED_SKIN", "ADAMANTINE_SKIN"),
                List.of("THICK_SKIN")
        ));
        register("skill_page_unmovable_mountain", new SkillModel(
                "UNMOVABLE_MOUNTAIN", null, "Montagne Immuable", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LIVING_FORTRESS", "ETERNAL_FORTRESS", "DIVINE_FORTRESS", "CELESTIAL_FORTRESS", "CREATOR_FORTRESS"),
                List.of("SOLID_STANCE")
        ));
        register("skill_page_battle_frenzy", new SkillModel(
                "BATTLE_FRENZY", null, "Frénésie de Combat", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("WAR_FRENZY", "BLOOD_FRENZY", "RAGE_FRENZY"),
                List.of("COMBAT_VIGOR")
        ));
        register("skill_page_survivor_spirit", new SkillModel(
                "SURVIVOR_SPIRIT", null, "Esprit du Survivant", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("SECOND_WIND")
        ));


        // --- MAGIQUE (Uncommon) ---
        register("skill_page_expanded_mind", new SkillModel(
                "EXPANDED_MIND", null, "Esprit Étendu", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BRILLIANT_MIND", "GENIUS_MIND", "COSMIC_MIND", "INFINITE_MIND", "CREATOR_MIND"),
                List.of("AWAKENED_MIND")
        ));
        register("skill_page_spellblade", new SkillModel(
                "SPELLBLADE", null, "Lame Enchantée", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_DEVASTATION", "ARCANE_ANNIHILATION", "ARCANE_OBLIVION", "ARCANE_GENESIS", "ARCANE_CREATION"),
                List.of("ARCANE_STRIKE")
        ));
        register("skill_page_arcane_shield", new SkillModel(
                "ARCANE_SHIELD", null, "Bouclier Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MANA_FORTRESS", "MANA_CITADEL"),
                List.of("MANA_BARRIER")
        ));
        register("skill_page_unleashed_magic", new SkillModel(
                "UNLEASHED_MAGIC", null, "Magie Déchaînée", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PURE_MAGIC", "ARCANE_SUPREMACY", "ABSOLUTE_SUPREMACY"),
                List.of("OVERFLOWING_POWER")
        ));
        register("skill_page_astral_cloak", new SkillModel(
                "ASTRAL_CLOAK", null, "Cape Astrale", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("MYSTIC_VEIL")
        ));

        // --- CHANCE (Uncommon) ---
        register("skill_page_critical_luck", new SkillModel(
                "CRITICAL_LUCK", null, "Chance Critique", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DESTINY_STRIKE", "DIVINE_STRIKE", "COSMIC_STRIKE", "GENESIS_STRIKE", "CREATOR_STRIKE"),
                List.of("LUCKY_STRIKE")
        ));
        register("skill_page_relic_hunter", new SkillModel(
                "RELIC_HUNTER", null, "Chasseur de Reliques", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARTIFACT_HUNTER", "LEGEND_HUNTER", "MYTH_HUNTER"),
                List.of("TREASURE_HUNTER")
        ));
        register("skill_page_fated_omen", new SkillModel(
                "FATED_OMEN", null, "Présage du Destin", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PROPHECY_OMEN", "COSMIC_OMEN"),
                List.of("GOOD_OMEN")
        ));
        register("skill_page_phantom_dodge", new SkillModel(
                "PHANTOM_DODGE", null, "Esquive Fantôme", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SHADOW_DODGE", "DIMENSIONAL_DODGE", "REALITY_DODGE", "TIME_DODGE", "FATE_DODGE"),
                List.of("MIRACLE_DODGE")
        ));
        register("skill_page_golden_touch", new SkillModel(
                "GOLDEN_TOUCH", null, "Toucher Doré", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("FORTUNE_COINS")
        ));

        // --- REGENERATION (Uncommon) ---
        register("skill_page_troll_blood", new SkillModel(
                "TROLL_BLOOD", null, "Sang de Troll", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HYDRA_BLOOD", "PHOENIX_BLOOD", "ETERNAL_BLOOD", "GENESIS_BLOOD", "CREATOR_BLOOD"),
                List.of("CELLULAR_REGENERATION")
        ));
        register("skill_page_iron_lungs", new SkillModel(
                "IRON_LUNGS", null, "Poumons de Fer", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ADAMANTINE_LUNGS", "MYTHRIL_LUNGS"),
                List.of("ACTIVE_BREATHING")
        ));
        register("skill_page_soul_stealer", new SkillModel(
                "SOUL_STEALER", null, "Voleur d'Âmes", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SPIRIT_DRAIN", "ARCANE_VAMPIRISM", "COSMIC_VAMPIRISM", "INFINITE_VAMPIRISM", "CREATOR_VAMPIRISM"),
                List.of("SPIRITUAL_SIPHON")
        ));
        register("skill_page_mana_stream", new SkillModel(
                "MANA_STREAM", null, "Flux de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MANA_RIVER", "MANA_OCEAN", "MANA_INFINITY"),
                List.of("MANA_FONT")
        ));
        register("skill_page_vital_recovery", new SkillModel(
                "VITAL_RECOVERY", null, "Rétablissement Vital", "all",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("NATURAL_RECOVERY")
        ));

        // --- RÉSISTANCE (Uncommon) ---
        register("skill_page_unyielding", new SkillModel(
                "UNYIELDING", null, "Inflexible", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNBREAKABLE", "INVINCIBLE", "IMMORTAL_ABSOLUTE", "CELESTIAL_IMMORTALITY", "TRUE_IMMORTALITY"),
                List.of("TENACITY")
        ));
        register("skill_page_reactive_bulwark", new SkillModel(
                "REACTIVE_BULWARK", null, "Rempart Réactif", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ADAMANTINE_BULWARK", "DIVINE_BULWARK", "COSMIC_BULWARK", "GENESIS_BULWARK", "CREATOR_BULWARK"),
                List.of("ADAPTIVE_SHIELD")
        ));
        register("skill_page_battle_scars", new SkillModel(
                "BATTLE_SCARS", null, "Cicatrices de Guerre", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("WAR_VETERAN", "WAR_LEGEND"),
                List.of("PAIN_TOLERANCE")
        ));
        register("skill_page_fortification", new SkillModel(
                "FORTIFICATION", null, "Fortification", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_FORTIFICATION", "MYTHRIL_FORTIFICATION", "ETERNAL_FORTIFICATION"),
                List.of("HARDENING")
        ));
        register("skill_page_iron_will", new SkillModel(
                "IRON_WILL", null, "Volonté de Fer", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("STEEL_NERVES")
        ));

        // --- VIE (Uncommon) ---
        register("skill_page_steel_constitution", new SkillModel(
                "STEEL_CONSTITUTION", null, "Constitution d'Acier", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TITAN_CONSTITUTION", "GOD_CONSTITUTION", "COSMIC_CONSTITUTION", "CELESTIAL_CONSTITUTION", "CREATOR_CONSTITUTION"),
                List.of("ROBUST_CONSTITUTION")
        ));
        register("skill_page_overflowing_life", new SkillModel(
                "OVERFLOWING_LIFE", null, "Vie Débordante", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BURSTING_LIFE", "ETERNAL_LIFE", "INFINITE_LIFE"),
                List.of("LIFE_FORCE")
        ));
        register("skill_page_enriched_blood", new SkillModel(
                "ENRICHED_BLOOD", null, "Sang Enrichi", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ANCIENT_BLOOD", "DRAGON_BLOOD"),
                List.of("VITAL_BLOOD")
        ));
        register("skill_page_heart_of_iron", new SkillModel(
                "HEART_OF_IRON", null, "Coeur de Fer", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HEART_OF_STEEL", "HEART_OF_DIAMOND", "HEART_OF_ETERNITY", "HEART_OF_GENESIS", "HEART_OF_CREATION"),
                List.of("HEART_OF_OAK")
        ));
        register("skill_page_undying", new SkillModel(
                "UNDYING", null, "Immortel", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("PERSEVERANCE")
        ));

        // --- UNCOMMON MAÎTRISE ---
        register("skill_page_axe_mastery", new SkillModel(
                "AXE_MASTERY", null, "Maîtrise de la Hache", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GREAT_AXE_MASTERY", "WAR_AXE_MASTERY", "LEGENDARY_AXE_MASTERY", "DIVIN_AXE_MASTERY", "CREATOR_AXE_MASTERY"),
                List.of()
        ));
        register("skill_page_bow_mastery", new SkillModel(
                "BOW_MASTERY", null, "Maîtrise de l'Arc", "archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MARKSMAN_MASTERY", "SNIPER_MASTERY", "LEGENDARY_SNIPER_MASTERY"),
                List.of()
        ));
        register("skill_page_spear_mastery", new SkillModel(
                "SPEAR_MASTERY", null, "Maîtrise de la Lance", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HALBERD_MASTERY", "DRAGON_SPEAR_MASTERY"),
                List.of()
        ));
        register("skill_page_dagger_mastery", new SkillModel(
                "DAGGER_MASTERY", null, "Maîtrise de la Dague", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SHADOW_BLADE_MASTERY", "ASSASSIN_BLADE_MASTERY", "LEGENDARY_DAGGER_MASTERY", "DIVIN_DAGGER_MASTERY", "CREATOR_DAGGER_MASTERY"),
                List.of()
        ));
        register("skill_page_minor_sword_mastery", new SkillModel(
                "MINOR_SWORD_MASTERY", null, "Initiation à l'Épée", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SWORD_MASTERY"),
                List.of()
        ));

        // =============================================
        // === RARE SKILLS (Évolutions Rares) ===
        // =============================================

        // --- RARE ATTACK ---
        register("skill_page_crimson_blade", new SkillModel(
                "CRIMSON_BLADE", null, "Lame Pourpre", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("VOID_BLADE", "ABYSS_BLADE", "GENESIS_EDGE", "CREATOR_EDGE"), List.of("DEEP_SLASH", "SHARP_BLADE")
        ));
        register("skill_page_warrior_swiftness", new SkillModel(
                "WARRIOR_SWIFTNESS", null, "Vivacité du Guerrier", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BERSERKER_SWIFTNESS", "DEMIGOD_SWIFTNESS", "GOD_SLAYER_SWIFTNESS", "CREATOR_SWIFTNESS"), List.of("DUELIST_SWIFTNESS", "COMBATANT_SWIFTNESS")
        ));
        register("skill_page_crushing_pressure", new SkillModel(
                "CRUSHING_PRESSURE", null, "Pression Écrasante", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ANNIHILATING_PRESSURE", "SOUL_CRUSHING_PRESSURE"), List.of("PRESSURE_POINT", "VITAL_PRESSURE")
        ));
        register("skill_page_blood_hunt", new SkillModel(
                "BLOOD_HUNT", null, "Traque Sanguinaire", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DEATH_HUNT"), List.of("HAUNTING_THRUST", "RELENTLESS_HUNT")
        ));

        // --- RARE DEFENSE ---
        register("skill_page_titan_resolve", new SkillModel(
                "TITAN_RESOLVE", null, "Résolution de Titan", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GOD_RESOLVE", "IMMORTAL_RESOLVE", "ETERNITY_RESOLVE", "ABSOLUTE_RESOLVE"), List.of("IRON_RESOLVE", "STEEL_RESOLVE")
        ));
        register("skill_page_master_parry", new SkillModel(
                "MASTER_PARRY", null, "Parade de Maître", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PERFECT_PARRY", "DIVINE_PARRY", "CELESTIAL_PARRY", "CREATOR_PARRY"), List.of("MINOR_PARRY", "EXPERT_PARRY")
        ));
        register("skill_page_monster_slayer_guard", new SkillModel(
                "MONSTER_SLAYER_GUARD", null, "Garde du Tueur", "warrior, archer, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DRAGON_SLAYER_GUARD"), List.of("HUNTER_GUARD", "BEAST_GUARD")
        ));
        register("skill_page_steel_body", new SkillModel(
                "STEEL_BODY", null, "Corps d'Acier", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIAMOND_BODY", "COSMIC_BODY"), List.of("STURDY_BODY", "IRON_BODY")
        ));

        // --- RARE AGILITÉ ---
        register("skill_page_storm_step", new SkillModel(
                "STORM_STEP", null, "Pas de Tempête", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("VOID_STEP", "DIMENSIONAL_STEP", "CELESTIAL_STEP", "CREATOR_STEP"), List.of("WIND_STEP", "GALE_STEP")
        ));
        register("skill_page_lightning_reflexes", new SkillModel(
                "LIGHTNING_REFLEXES", null, "Réflexes de Foudre", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_REFLEXES", "COSMIC_REFLEXES"), List.of("LIGHT_REFLEXES", "THUNDER_REFLEXES")
        ));
        register("skill_page_acrobatic_poise", new SkillModel(
                "ACROBATIC_POISE", null, "Souplesse Acrobatique", "novice, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GRAVITY_DEFIANCE"), List.of("ELDANIOR_SUPPLENESS", "CATLIKE_POISE")
        ));
        register("skill_page_deadly_precision", new SkillModel(
                "DEADLY_PRECISION", null, "Précision Mortelle", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FATAL_PRECISION", "ABSOLUTE_PRECISION", "OMNISCIENT_PRECISION", "CREATOR_PRECISION"), List.of("KEEN_SENSES", "RAZOR_SENSES")
        ));

        // --- RARE DETECTION ---
        register("skill_page_eagle_vision", new SkillModel(
                "EAGLE_VISION", null, "Vision d'Aigle", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("OMNISCIENT_VISION", "ALL_SEEING_EYE", "GENESIS_VISION", "CREATOR_VISION"), List.of("EAGLE_EYE", "HAWK_EYE")
        ));
        register("skill_page_combat_intuition", new SkillModel(
                "COMBAT_INTUITION", null, "Intuition de Combat", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("WAR_PROPHECY", "FATE_VISION"), List.of("SURVIVAL_INSTINCT", "DANGER_SENSE")
        ));
        register("skill_page_abyssal_vision", new SkillModel(
                "ABYSSAL_VISION", null, "Vision Abyssale", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("VOID_SIGHT"), List.of("NIGHT_VISION", "DARK_VISION")
        ));
        register("skill_page_mind_reader", new SkillModel(
                "MIND_READER", null, "Lecture Mentale", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SOUL_READER", "TRUE_SIGHT", "OMNI_SIGHT", "ABSOLUTE_SIGHT"), List.of("SIXTH_SENSE", "PSYCHIC_AWARENESS")
        ));

        // --- RARE ENDURANCE ---
        register("skill_page_dragon_lungs", new SkillModel(
                "DRAGON_LUNGS", null, "Poumons de Dragon", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TITAN_LUNGS", "COSMIC_LUNGS", "CELESTIAL_LUNGS", "CREATOR_LUNGS"), List.of("TIRELESS_BREATH", "LUNGS_OF_STEEL")
        ));
        register("skill_page_fortified_skin", new SkillModel(
                "FORTIFIED_SKIN", null, "Peau Fortifiée", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ADAMANTINE_SKIN"), List.of("THICK_SKIN", "ARMORED_SKIN")
        ));
        register("skill_page_living_fortress", new SkillModel(
                "LIVING_FORTRESS", null, "Forteresse Vivante", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ETERNAL_FORTRESS", "DIVINE_FORTRESS", "CELESTIAL_FORTRESS", "CREATOR_FORTRESS"), List.of("SOLID_STANCE", "UNMOVABLE_MOUNTAIN")
        ));
        register("skill_page_war_frenzy", new SkillModel(
                "WAR_FRENZY", null, "Frénésie Guerrière", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BLOOD_FRENZY", "RAGE_FRENZY"), List.of("COMBAT_VIGOR", "BATTLE_FRENZY")
        ));

        // --- RARE MAGIQUE ---
        register("skill_page_brilliant_mind", new SkillModel(
                "BRILLIANT_MIND", null, "Esprit Brillant", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GENIUS_MIND", "COSMIC_MIND", "INFINITE_MIND", "CREATOR_MIND"), List.of("AWAKENED_MIND", "EXPANDED_MIND")
        ));
        register("skill_page_arcane_devastation", new SkillModel(
                "ARCANE_DEVASTATION", null, "Dévastation Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_ANNIHILATION", "ARCANE_OBLIVION", "ARCANE_GENESIS", "ARCANE_CREATION"), List.of("ARCANE_STRIKE", "SPELLBLADE")
        ));
        register("skill_page_mana_fortress", new SkillModel(
                "MANA_FORTRESS", null, "Forteresse de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MANA_CITADEL"), List.of("MANA_BARRIER", "ARCANE_SHIELD")
        ));
        register("skill_page_pure_magic", new SkillModel(
                "PURE_MAGIC", null, "Magie Pure", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_SUPREMACY", "ABSOLUTE_SUPREMACY"), List.of("OVERFLOWING_POWER", "UNLEASHED_MAGIC")
        ));

        // --- RARE CHANCE ---
        register("skill_page_destiny_strike", new SkillModel(
                "DESTINY_STRIKE", null, "Frappe du Destin", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_STRIKE", "COSMIC_STRIKE", "GENESIS_STRIKE", "CREATOR_STRIKE"), List.of("LUCKY_STRIKE", "CRITICAL_LUCK")
        ));
        register("skill_page_artifact_hunter", new SkillModel(
                "ARTIFACT_HUNTER", null, "Chasseur d'Artefacts", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LEGEND_HUNTER", "MYTH_HUNTER"), List.of("TREASURE_HUNTER", "RELIC_HUNTER")
        ));
        register("skill_page_prophecy_omen", new SkillModel(
                "PROPHECY_OMEN", null, "Présage Prophétique", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_OMEN"), List.of("GOOD_OMEN", "FATED_OMEN")
        ));
        register("skill_page_shadow_dodge", new SkillModel(
                "SHADOW_DODGE", null, "Esquive de l'Ombre", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIMENSIONAL_DODGE", "REALITY_DODGE", "TIME_DODGE", "FATE_DODGE"), List.of("MIRACLE_DODGE", "PHANTOM_DODGE")
        ));

        // --- RARE REGENERATION ---
        register("skill_page_hydra_blood", new SkillModel(
                "HYDRA_BLOOD", null, "Sang d'Hydre", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PHOENIX_BLOOD", "ETERNAL_BLOOD", "GENESIS_BLOOD", "CREATOR_BLOOD"), List.of("CELLULAR_REGENERATION", "TROLL_BLOOD")
        ));
        register("skill_page_adamantine_lungs", new SkillModel(
                "ADAMANTINE_LUNGS", null, "Poumons d'Adamantine", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MYTHRIL_LUNGS"), List.of("ACTIVE_BREATHING", "IRON_LUNGS")
        ));
        register("skill_page_spirit_drain", new SkillModel(
                "SPIRIT_DRAIN", null, "Drain Spirituel", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_VAMPIRISM", "COSMIC_VAMPIRISM", "INFINITE_VAMPIRISM", "CREATOR_VAMPIRISM"), List.of("SPIRITUAL_SIPHON", "SOUL_STEALER")
        ));
        register("skill_page_mana_river", new SkillModel(
                "MANA_RIVER", null, "Rivière de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MANA_OCEAN", "MANA_INFINITY"), List.of("MANA_FONT", "MANA_STREAM")
        ));

        // --- RARE RÉSISTANCE ---
        register("skill_page_unbreakable", new SkillModel(
                "UNBREAKABLE", null, "Incassable", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("INVINCIBLE", "IMMORTAL_ABSOLUTE", "CELESTIAL_IMMORTALITY", "TRUE_IMMORTALITY"), List.of("TENACITY", "UNYIELDING")
        ));
        register("skill_page_adamantine_bulwark", new SkillModel(
                "ADAMANTINE_BULWARK", null, "Rempart d'Adamantine", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_BULWARK", "COSMIC_BULWARK", "GENESIS_BULWARK", "CREATOR_BULWARK"), List.of("ADAPTIVE_SHIELD", "REACTIVE_BULWARK")
        ));
        register("skill_page_war_veteran", new SkillModel(
                "WAR_VETERAN", null, "Vétéran de Guerre", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("WAR_LEGEND"), List.of("PAIN_TOLERANCE", "BATTLE_SCARS")
        ));
        register("skill_page_iron_fortification", new SkillModel(
                "IRON_FORTIFICATION", null, "Fortification de Fer", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MYTHRIL_FORTIFICATION", "ETERNAL_FORTIFICATION"), List.of("HARDENING", "FORTIFICATION")
        ));

        // --- RARE VIE ---
        register("skill_page_titan_constitution", new SkillModel(
                "TITAN_CONSTITUTION", null, "Constitution de Titan", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GOD_CONSTITUTION", "COSMIC_CONSTITUTION", "CELESTIAL_CONSTITUTION", "CREATOR_CONSTITUTION"), List.of("ROBUST_CONSTITUTION", "STEEL_CONSTITUTION")
        ));
        register("skill_page_bursting_life", new SkillModel(
                "BURSTING_LIFE", null, "Vie Explosive", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ETERNAL_LIFE", "INFINITE_LIFE"), List.of("LIFE_FORCE", "OVERFLOWING_LIFE")
        ));
        register("skill_page_ancient_blood", new SkillModel(
                "ANCIENT_BLOOD", null, "Sang Ancestral", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DRAGON_BLOOD"), List.of("VITAL_BLOOD", "ENRICHED_BLOOD")
        ));
        register("skill_page_heart_of_steel", new SkillModel(
                "HEART_OF_STEEL", null, "Coeur d'Acier", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HEART_OF_DIAMOND", "HEART_OF_ETERNITY", "HEART_OF_GENESIS", "HEART_OF_CREATION"), List.of("HEART_OF_OAK", "HEART_OF_IRON")
        ));

        // --- RARE MAÎTRISE ---
        register("skill_page_great_axe_mastery", new SkillModel(
                "GREAT_AXE_MASTERY", null, "Maîtrise de la Grande Hache", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("WAR_AXE_MASTERY", "LEGENDARY_AXE_MASTERY", "DIVIN_AXE_MASTERY", "CREATOR_AXE_MASTERY"), List.of("AXE_MASTERY")
        ));
        register("skill_page_marksman_mastery", new SkillModel(
                "MARKSMAN_MASTERY", null, "Maîtrise du Tir", "archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SNIPER_MASTERY", "LEGENDARY_SNIPER_MASTERY"), List.of("BOW_MASTERY")
        ));
        register("skill_page_halberd_mastery", new SkillModel(
                "HALBERD_MASTERY", null, "Maîtrise de la Hallebarde", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DRAGON_SPEAR_MASTERY"), List.of("SPEAR_MASTERY")
        ));
        register("skill_page_shadow_blade_mastery", new SkillModel(
                "SHADOW_BLADE_MASTERY", null, "Maîtrise de la Lame d'Ombre", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ASSASSIN_BLADE_MASTERY", "LEGENDARY_DAGGER_MASTERY", "DIVIN_DAGGER_MASTERY", "CREATOR_DAGGER_MASTERY"), List.of("DAGGER_MASTERY")
        ));

        // =============================================
        // === ÉPIQUE SKILLS (Évolutions Épiques) ===
        // =============================================

        // --- ÉPIQUE ATTACK ---
        register("skill_page_void_blade", new SkillModel(
                "VOID_BLADE", null, "Lame du Néant", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ABYSS_BLADE", "GENESIS_EDGE", "CREATOR_EDGE"), List.of("DEEP_SLASH", "SHARP_BLADE", "CRIMSON_BLADE")
        ));
        register("skill_page_berserker_swiftness", new SkillModel(
                "BERSERKER_SWIFTNESS", null, "Vivacité du Berserker", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DEMIGOD_SWIFTNESS", "GOD_SLAYER_SWIFTNESS", "CREATOR_SWIFTNESS"), List.of("DUELIST_SWIFTNESS", "COMBATANT_SWIFTNESS", "WARRIOR_SWIFTNESS")
        ));
        register("skill_page_annihilating_pressure", new SkillModel(
                "ANNIHILATING_PRESSURE", null, "Pression Annihilante", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SOUL_CRUSHING_PRESSURE"), List.of("PRESSURE_POINT", "VITAL_PRESSURE", "CRUSHING_PRESSURE")
        ));
        register("skill_page_death_hunt", new SkillModel(
                "DEATH_HUNT", null, "Traque Mortelle", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("HAUNTING_THRUST", "RELENTLESS_HUNT", "BLOOD_HUNT")
        ));

        // --- ÉPIQUE DEFENSE ---
        register("skill_page_god_resolve", new SkillModel(
                "GOD_RESOLVE", null, "Résolution Divine", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IMMORTAL_RESOLVE", "ETERNITY_RESOLVE", "ABSOLUTE_RESOLVE"), List.of("IRON_RESOLVE", "STEEL_RESOLVE", "TITAN_RESOLVE")
        ));
        register("skill_page_perfect_parry", new SkillModel(
                "PERFECT_PARRY", null, "Parade Parfaite", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_PARRY", "CELESTIAL_PARRY", "CREATOR_PARRY"), List.of("MINOR_PARRY", "EXPERT_PARRY", "MASTER_PARRY")
        ));
        register("skill_page_dragon_slayer_guard", new SkillModel(
                "DRAGON_SLAYER_GUARD", null, "Garde du Tueur de Dragon", "warrior, archer, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("HUNTER_GUARD", "BEAST_GUARD", "MONSTER_SLAYER_GUARD")
        ));
        register("skill_page_diamond_body", new SkillModel(
                "DIAMOND_BODY", null, "Corps de Diamant", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_BODY"), List.of("STURDY_BODY", "IRON_BODY", "STEEL_BODY")
        ));

        // --- ÉPIQUE AGILITÉ ---
        register("skill_page_void_step", new SkillModel(
                "VOID_STEP", null, "Pas du Néant", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIMENSIONAL_STEP", "CELESTIAL_STEP", "CREATOR_STEP"), List.of("WIND_STEP", "GALE_STEP", "STORM_STEP")
        ));
        register("skill_page_divine_reflexes", new SkillModel(
                "DIVINE_REFLEXES", null, "Réflexes Divins", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_REFLEXES"), List.of("LIGHT_REFLEXES", "THUNDER_REFLEXES", "LIGHTNING_REFLEXES")
        ));
        register("skill_page_gravity_defiance", new SkillModel(
                "GRAVITY_DEFIANCE", null, "Défi à la Gravité", "novice, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("ELDANIOR_SUPPLENESS", "CATLIKE_POISE", "ACROBATIC_POISE")
        ));
        register("skill_page_fatal_precision", new SkillModel(
                "FATAL_PRECISION", null, "Précision Fatale", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ABSOLUTE_PRECISION", "OMNISCIENT_PRECISION", "CREATOR_PRECISION"), List.of("KEEN_SENSES", "RAZOR_SENSES", "DEADLY_PRECISION")
        ));

        // --- ÉPIQUE DETECTION ---
        register("skill_page_omniscient_vision", new SkillModel(
                "OMNISCIENT_VISION", null, "Vision Omnisciente", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ALL_SEEING_EYE", "GENESIS_VISION", "CREATOR_VISION"), List.of("EAGLE_EYE", "HAWK_EYE", "EAGLE_VISION")
        ));
        register("skill_page_war_prophecy", new SkillModel(
                "WAR_PROPHECY", null, "Prophétie de Guerre", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FATE_VISION"), List.of("SURVIVAL_INSTINCT", "DANGER_SENSE", "COMBAT_INTUITION")
        ));
        register("skill_page_void_sight", new SkillModel(
                "VOID_SIGHT", null, "Vue du Néant", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("NIGHT_VISION", "DARK_VISION", "ABYSSAL_VISION")
        ));
        register("skill_page_soul_reader", new SkillModel(
                "SOUL_READER", null, "Lecture d'Âme", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TRUE_SIGHT", "OMNI_SIGHT", "ABSOLUTE_SIGHT"), List.of("SIXTH_SENSE", "PSYCHIC_AWARENESS", "MIND_READER")
        ));

        // --- ÉPIQUE ENDURANCE ---
        register("skill_page_titan_lungs", new SkillModel(
                "TITAN_LUNGS", null, "Poumons de Titan", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_LUNGS", "CELESTIAL_LUNGS", "CREATOR_LUNGS"), List.of("TIRELESS_BREATH", "LUNGS_OF_STEEL", "DRAGON_LUNGS")
        ));
        register("skill_page_adamantine_skin", new SkillModel(
                "ADAMANTINE_SKIN", null, "Peau d'Adamantine", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("THICK_SKIN", "ARMORED_SKIN", "FORTIFIED_SKIN")
        ));
        register("skill_page_eternal_fortress", new SkillModel(
                "ETERNAL_FORTRESS", null, "Forteresse Éternelle", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_FORTRESS", "CELESTIAL_FORTRESS", "CREATOR_FORTRESS"), List.of("SOLID_STANCE", "UNMOVABLE_MOUNTAIN", "LIVING_FORTRESS")
        ));
        register("skill_page_blood_frenzy", new SkillModel(
                "BLOOD_FRENZY", null, "Frénésie Sanguinaire", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("RAGE_FRENZY"), List.of("COMBAT_VIGOR", "BATTLE_FRENZY", "WAR_FRENZY")
        ));

        // --- ÉPIQUE MAGIQUE ---
        register("skill_page_genius_mind", new SkillModel(
                "GENIUS_MIND", null, "Esprit de Génie", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_MIND", "INFINITE_MIND", "CREATOR_MIND"), List.of("AWAKENED_MIND", "EXPANDED_MIND", "BRILLIANT_MIND")
        ));
        register("skill_page_arcane_annihilation", new SkillModel(
                "ARCANE_ANNIHILATION", null, "Annihilation Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_OBLIVION", "ARCANE_GENESIS", "ARCANE_CREATION"), List.of("ARCANE_STRIKE", "SPELLBLADE", "ARCANE_DEVASTATION")
        ));
        register("skill_page_mana_citadel", new SkillModel(
                "MANA_CITADEL", null, "Citadelle de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("MANA_BARRIER", "ARCANE_SHIELD", "MANA_FORTRESS")
        ));
        register("skill_page_arcane_supremacy", new SkillModel(
                "ARCANE_SUPREMACY", null, "Suprématie Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ABSOLUTE_SUPREMACY"), List.of("OVERFLOWING_POWER", "UNLEASHED_MAGIC", "PURE_MAGIC")
        ));

        // --- ÉPIQUE CHANCE ---
        register("skill_page_divine_strike", new SkillModel(
                "DIVINE_STRIKE", null, "Frappe Divine", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_STRIKE", "GENESIS_STRIKE", "CREATOR_STRIKE"), List.of("LUCKY_STRIKE", "CRITICAL_LUCK", "DESTINY_STRIKE")
        ));
        register("skill_page_legend_hunter", new SkillModel(
                "LEGEND_HUNTER", null, "Chasseur de Légendes", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MYTH_HUNTER"), List.of("TREASURE_HUNTER", "RELIC_HUNTER", "ARTIFACT_HUNTER")
        ));
        register("skill_page_cosmic_omen", new SkillModel(
                "COSMIC_OMEN", null, "Présage Cosmique", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("GOOD_OMEN", "FATED_OMEN", "PROPHECY_OMEN")
        ));
        register("skill_page_dimensional_dodge", new SkillModel(
                "DIMENSIONAL_DODGE", null, "Esquive Dimensionnelle", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("REALITY_DODGE", "TIME_DODGE", "FATE_DODGE"), List.of("MIRACLE_DODGE", "PHANTOM_DODGE", "SHADOW_DODGE")
        ));

        // --- ÉPIQUE REGENERATION ---
        register("skill_page_phoenix_blood", new SkillModel(
                "PHOENIX_BLOOD", null, "Sang de Phénix", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ETERNAL_BLOOD", "GENESIS_BLOOD", "CREATOR_BLOOD"), List.of("CELLULAR_REGENERATION", "TROLL_BLOOD", "HYDRA_BLOOD")
        ));
        register("skill_page_mythril_lungs", new SkillModel(
                "MYTHRIL_LUNGS", null, "Poumons de Mythril", "assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("ACTIVE_BREATHING", "IRON_LUNGS", "ADAMANTINE_LUNGS")
        ));
        register("skill_page_arcane_vampirism", new SkillModel(
                "ARCANE_VAMPIRISM", null, "Vampirisme Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_VAMPIRISM", "INFINITE_VAMPIRISM", "CREATOR_VAMPIRISM"), List.of("SPIRITUAL_SIPHON", "SOUL_STEALER", "SPIRIT_DRAIN")
        ));
        register("skill_page_mana_ocean", new SkillModel(
                "MANA_OCEAN", null, "Océan de Mana", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("MANA_INFINITY"), List.of("MANA_FONT", "MANA_STREAM", "MANA_RIVER")
        ));

        // --- ÉPIQUE RÉSISTANCE ---
        register("skill_page_invincible", new SkillModel(
                "INVINCIBLE", null, "Invincible", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IMMORTAL_ABSOLUTE", "CELESTIAL_IMMORTALITY", "TRUE_IMMORTALITY"), List.of("TENACITY", "UNYIELDING", "UNBREAKABLE")
        ));
        register("skill_page_divine_bulwark", new SkillModel(
                "DIVINE_BULWARK", null, "Rempart Divin", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_BULWARK", "GENESIS_BULWARK", "CREATOR_BULWARK"), List.of("ADAPTIVE_SHIELD", "REACTIVE_BULWARK", "ADAMANTINE_BULWARK")
        ));
        register("skill_page_war_legend", new SkillModel(
                "WAR_LEGEND", null, "Légende de Guerre", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("PAIN_TOLERANCE", "BATTLE_SCARS", "WAR_VETERAN")
        ));
        register("skill_page_mythril_fortification", new SkillModel(
                "MYTHRIL_FORTIFICATION", null, "Fortification de Mythril", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ETERNAL_FORTIFICATION"), List.of("HARDENING", "FORTIFICATION", "IRON_FORTIFICATION")
        ));

        // --- ÉPIQUE VIE ---
        register("skill_page_god_constitution", new SkillModel(
                "GOD_CONSTITUTION", null, "Constitution Divine", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("COSMIC_CONSTITUTION", "CELESTIAL_CONSTITUTION", "CREATOR_CONSTITUTION"), List.of("ROBUST_CONSTITUTION", "STEEL_CONSTITUTION", "TITAN_CONSTITUTION")
        ));
        register("skill_page_eternal_life", new SkillModel(
                "ETERNAL_LIFE", null, "Vie Éternelle", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("INFINITE_LIFE"), List.of("LIFE_FORCE", "OVERFLOWING_LIFE", "BURSTING_LIFE")
        ));
        register("skill_page_dragon_blood", new SkillModel(
                "DRAGON_BLOOD", null, "Sang de Dragon", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("VITAL_BLOOD", "ENRICHED_BLOOD", "ANCIENT_BLOOD")
        ));
        register("skill_page_heart_of_diamond", new SkillModel(
                "HEART_OF_DIAMOND", null, "Coeur de Diamant", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HEART_OF_ETERNITY", "HEART_OF_GENESIS", "HEART_OF_CREATION"), List.of("HEART_OF_OAK", "HEART_OF_IRON", "HEART_OF_STEEL")
        ));

        // --- ÉPIQUE MAÎTRISE ---
        register("skill_page_war_axe_mastery", new SkillModel(
                "WAR_AXE_MASTERY", null, "Maîtrise de la Hache de Guerre", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LEGENDARY_AXE_MASTERY", "DIVIN_AXE_MASTERY", "CREATOR_AXE_MASTERY"), List.of("AXE_MASTERY", "GREAT_AXE_MASTERY")
        ));
        register("skill_page_sniper_mastery", new SkillModel(
                "SNIPER_MASTERY", null, "Maîtrise du Sniper", "archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LEGENDARY_SNIPER_MASTERY"), List.of("BOW_MASTERY", "MARKSMAN_MASTERY")
        ));
        register("skill_page_dragon_spear_mastery", new SkillModel(
                "DRAGON_SPEAR_MASTERY", null, "Maîtrise de la Lance Dragon", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SPEAR_MASTERY", "HALBERD_MASTERY")
        ));
        register("skill_page_assassin_blade_mastery", new SkillModel(
                "ASSASSIN_BLADE_MASTERY", null, "Maîtrise de la Lame d'Assassin", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LEGENDARY_DAGGER_MASTERY", "DIVIN_DAGGER_MASTERY", "CREATOR_DAGGER_MASTERY"), List.of("DAGGER_MASTERY", "SHADOW_BLADE_MASTERY")
        ));

        // =============================================
        // === UNIQUE SKILLS (Évolutions Uniques) ===
        // =============================================

        // --- UNIQUE ATTACK ---
        register("skill_page_abyss_blade", new SkillModel(
                "ABYSS_BLADE", null, "Lame de l'Abîme", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GENESIS_EDGE", "CREATOR_EDGE"), List.of("DEEP_SLASH", "SHARP_BLADE", "CRIMSON_BLADE", "VOID_BLADE")
        ));
        register("skill_page_demigod_swiftness", new SkillModel(
                "DEMIGOD_SWIFTNESS", null, "Vivacité du Demi-Dieu", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GOD_SLAYER_SWIFTNESS", "CREATOR_SWIFTNESS"), List.of("DUELIST_SWIFTNESS", "COMBATANT_SWIFTNESS", "WARRIOR_SWIFTNESS", "BERSERKER_SWIFTNESS")
        ));
        register("skill_page_soul_crushing_pressure", new SkillModel(
                "SOUL_CRUSHING_PRESSURE", null, "Pression Brise-Âme", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("PRESSURE_POINT", "VITAL_PRESSURE", "CRUSHING_PRESSURE", "ANNIHILATING_PRESSURE")
        ));

        // --- UNIQUE DEFENSE ---
        register("skill_page_immortal_resolve", new SkillModel(
                "IMMORTAL_RESOLVE", null, "Résolution Immortelle", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ETERNITY_RESOLVE", "ABSOLUTE_RESOLVE"), List.of("IRON_RESOLVE", "STEEL_RESOLVE", "TITAN_RESOLVE", "GOD_RESOLVE")
        ));
        register("skill_page_divine_parry", new SkillModel(
                "DIVINE_PARRY", null, "Parade Divine", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CELESTIAL_PARRY", "CREATOR_PARRY"), List.of("MINOR_PARRY", "EXPERT_PARRY", "MASTER_PARRY", "PERFECT_PARRY")
        ));
        register("skill_page_cosmic_body", new SkillModel(
                "COSMIC_BODY", null, "Corps Cosmique", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("STURDY_BODY", "IRON_BODY", "STEEL_BODY", "DIAMOND_BODY")
        ));

        // --- UNIQUE AGILITÉ ---
        register("skill_page_dimensional_step", new SkillModel(
                "DIMENSIONAL_STEP", null, "Pas Dimensionnel", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CELESTIAL_STEP", "CREATOR_STEP"), List.of("WIND_STEP", "GALE_STEP", "STORM_STEP", "VOID_STEP")
        ));
        register("skill_page_cosmic_reflexes", new SkillModel(
                "COSMIC_REFLEXES", null, "Réflexes Cosmiques", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("LIGHT_REFLEXES", "THUNDER_REFLEXES", "LIGHTNING_REFLEXES", "DIVINE_REFLEXES")
        ));
        register("skill_page_absolute_precision", new SkillModel(
                "ABSOLUTE_PRECISION", null, "Précision Absolue", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("OMNISCIENT_PRECISION", "CREATOR_PRECISION"), List.of("KEEN_SENSES", "RAZOR_SENSES", "DEADLY_PRECISION", "FATAL_PRECISION")
        ));

        // --- UNIQUE DETECTION ---
        register("skill_page_all_seeing_eye", new SkillModel(
                "ALL_SEEING_EYE", null, "Oeil Omniscient", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GENESIS_VISION", "CREATOR_VISION"), List.of("EAGLE_EYE", "HAWK_EYE", "EAGLE_VISION", "OMNISCIENT_VISION")
        ));
        register("skill_page_fate_vision", new SkillModel(
                "FATE_VISION", null, "Vision du Destin", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SURVIVAL_INSTINCT", "DANGER_SENSE", "COMBAT_INTUITION", "WAR_PROPHECY")
        ));
        register("skill_page_true_sight", new SkillModel(
                "TRUE_SIGHT", null, "Vision Véritable", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("OMNI_SIGHT", "ABSOLUTE_SIGHT"), List.of("SIXTH_SENSE", "PSYCHIC_AWARENESS", "MIND_READER", "SOUL_READER")
        ));

        // --- UNIQUE ENDURANCE ---
        register("skill_page_cosmic_lungs", new SkillModel(
                "COSMIC_LUNGS", null, "Poumons Cosmiques", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CELESTIAL_LUNGS", "CREATOR_LUNGS"), List.of("TIRELESS_BREATH", "LUNGS_OF_STEEL", "DRAGON_LUNGS", "TITAN_LUNGS")
        ));
        register("skill_page_divine_fortress", new SkillModel(
                "DIVINE_FORTRESS", null, "Forteresse Divine", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CELESTIAL_FORTRESS", "CREATOR_FORTRESS"), List.of("SOLID_STANCE", "UNMOVABLE_MOUNTAIN", "LIVING_FORTRESS", "ETERNAL_FORTRESS")
        ));
        register("skill_page_rage_frenzy", new SkillModel(
                "RAGE_FRENZY", null, "Frénésie Enragée", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("COMBAT_VIGOR", "BATTLE_FRENZY", "WAR_FRENZY", "BLOOD_FRENZY")
        ));

        // --- UNIQUE MAGIQUE ---
        register("skill_page_cosmic_mind", new SkillModel(
                "COSMIC_MIND", null, "Esprit Cosmique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("INFINITE_MIND", "CREATOR_MIND"), List.of("AWAKENED_MIND", "EXPANDED_MIND", "BRILLIANT_MIND", "GENIUS_MIND")
        ));
        register("skill_page_arcane_oblivion", new SkillModel(
                "ARCANE_OBLIVION", null, "Oubli Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_GENESIS", "ARCANE_CREATION"), List.of("ARCANE_STRIKE", "SPELLBLADE", "ARCANE_DEVASTATION", "ARCANE_ANNIHILATION")
        ));
        register("skill_page_absolute_supremacy", new SkillModel(
                "ABSOLUTE_SUPREMACY", null, "Suprématie Absolue", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("OVERFLOWING_POWER", "UNLEASHED_MAGIC", "PURE_MAGIC", "ARCANE_SUPREMACY")
        ));

        // --- UNIQUE CHANCE ---
        register("skill_page_cosmic_strike", new SkillModel(
                "COSMIC_STRIKE", null, "Frappe Cosmique", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GENESIS_STRIKE", "CREATOR_STRIKE"), List.of("LUCKY_STRIKE", "CRITICAL_LUCK", "DESTINY_STRIKE", "DIVINE_STRIKE")
        ));
        register("skill_page_myth_hunter", new SkillModel(
                "MYTH_HUNTER", null, "Chasseur de Mythes", "merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("TREASURE_HUNTER", "RELIC_HUNTER", "ARTIFACT_HUNTER", "LEGEND_HUNTER")
        ));
        register("skill_page_reality_dodge", new SkillModel(
                "REALITY_DODGE", null, "Esquive de la Réalité", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TIME_DODGE", "FATE_DODGE"), List.of("MIRACLE_DODGE", "PHANTOM_DODGE", "SHADOW_DODGE", "DIMENSIONAL_DODGE")
        ));

        // --- UNIQUE REGENERATION ---
        register("skill_page_eternal_blood", new SkillModel(
                "ETERNAL_BLOOD", null, "Sang Éternel", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GENESIS_BLOOD", "CREATOR_BLOOD"), List.of("CELLULAR_REGENERATION", "TROLL_BLOOD", "HYDRA_BLOOD", "PHOENIX_BLOOD")
        ));
        register("skill_page_cosmic_vampirism", new SkillModel(
                "COSMIC_VAMPIRISM", null, "Vampirisme Cosmique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("INFINITE_VAMPIRISM", "CREATOR_VAMPIRISM"), List.of("SPIRITUAL_SIPHON", "SOUL_STEALER", "SPIRIT_DRAIN", "ARCANE_VAMPIRISM")
        ));
        register("skill_page_mana_infinity", new SkillModel(
                "MANA_INFINITY", null, "Mana Infini", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("MANA_FONT", "MANA_STREAM", "MANA_RIVER", "MANA_OCEAN")
        ));

        // --- UNIQUE RÉSISTANCE ---
        register("skill_page_immortal_absolute", new SkillModel(
                "IMMORTAL_ABSOLUTE", null, "Immortel Absolu", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CELESTIAL_IMMORTALITY", "TRUE_IMMORTALITY"), List.of("TENACITY", "UNYIELDING", "UNBREAKABLE", "INVINCIBLE")
        ));
        register("skill_page_cosmic_bulwark", new SkillModel(
                "COSMIC_BULWARK", null, "Rempart Cosmique", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("GENESIS_BULWARK", "CREATOR_BULWARK"), List.of("ADAPTIVE_SHIELD", "REACTIVE_BULWARK", "ADAMANTINE_BULWARK", "DIVINE_BULWARK")
        ));
        register("skill_page_eternal_fortification", new SkillModel(
                "ETERNAL_FORTIFICATION", null, "Fortification Éternelle", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("HARDENING", "FORTIFICATION", "IRON_FORTIFICATION", "MYTHRIL_FORTIFICATION")
        ));

        // --- UNIQUE VIE ---
        register("skill_page_cosmic_constitution", new SkillModel(
                "COSMIC_CONSTITUTION", null, "Constitution Cosmique", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CELESTIAL_CONSTITUTION", "CREATOR_CONSTITUTION"), List.of("ROBUST_CONSTITUTION", "STEEL_CONSTITUTION", "TITAN_CONSTITUTION", "GOD_CONSTITUTION")
        ));
        register("skill_page_infinite_life", new SkillModel(
                "INFINITE_LIFE", null, "Vie Infinie", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("LIFE_FORCE", "OVERFLOWING_LIFE", "BURSTING_LIFE", "ETERNAL_LIFE")
        ));
        register("skill_page_heart_of_eternity", new SkillModel(
                "HEART_OF_ETERNITY", null, "Coeur d'Éternité", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HEART_OF_GENESIS", "HEART_OF_CREATION"), List.of("HEART_OF_OAK", "HEART_OF_IRON", "HEART_OF_STEEL", "HEART_OF_DIAMOND")
        ));

        // --- UNIQUE MAÎTRISE ---
        register("skill_page_legendary_axe_mastery", new SkillModel(
                "LEGENDARY_AXE_MASTERY", null, "Maîtrise Légendaire de la Hache", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVIN_AXE_MASTERY", "CREATOR_AXE_MASTERY"), List.of("AXE_MASTERY", "GREAT_AXE_MASTERY", "WAR_AXE_MASTERY")
        ));
        register("skill_page_legendary_sniper_mastery", new SkillModel(
                "LEGENDARY_SNIPER_MASTERY", null, "Maîtrise Légendaire du Tir", "archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("BOW_MASTERY", "MARKSMAN_MASTERY", "SNIPER_MASTERY")
        ));
        register("skill_page_legendary_dagger_mastery", new SkillModel(
                "LEGENDARY_DAGGER_MASTERY", null, "Maîtrise Légendaire de la Dague", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVIN_DAGGER_MASTERY", "CREATOR_DAGGER_MASTERY"), List.of("DAGGER_MASTERY", "SHADOW_BLADE_MASTERY", "ASSASSIN_BLADE_MASTERY")
        ));

        // =============================================
        // === LÉGENDAIRE SKILLS (Évolutions Légendaires) ===
        // =============================================

        // --- LÉGENDAIRE ATTACK ---
        register("skill_page_genesis_edge", new SkillModel(
                "GENESIS_EDGE", null, "Tranchant de la Genèse", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_EDGE"), List.of("DEEP_SLASH", "SHARP_BLADE", "CRIMSON_BLADE", "VOID_BLADE", "ABYSS_BLADE")
        ));
        register("skill_page_god_slayer_swiftness", new SkillModel(
                "GOD_SLAYER_SWIFTNESS", null, "Vivacité du Tueur de Dieux", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_SWIFTNESS"), List.of("DUELIST_SWIFTNESS", "COMBATANT_SWIFTNESS", "WARRIOR_SWIFTNESS", "BERSERKER_SWIFTNESS", "DEMIGOD_SWIFTNESS")
        ));

        // --- LÉGENDAIRE DEFENSE ---
        register("skill_page_eternity_resolve", new SkillModel(
                "ETERNITY_RESOLVE", null, "Résolution Éternelle", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ABSOLUTE_RESOLVE"), List.of("IRON_RESOLVE", "STEEL_RESOLVE", "TITAN_RESOLVE", "GOD_RESOLVE", "IMMORTAL_RESOLVE")
        ));
        register("skill_page_celestial_parry", new SkillModel(
                "CELESTIAL_PARRY", null, "Parade Céleste", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_PARRY"), List.of("MINOR_PARRY", "EXPERT_PARRY", "MASTER_PARRY", "PERFECT_PARRY", "DIVINE_PARRY")
        ));

        // --- LÉGENDAIRE AGILITÉ ---
        register("skill_page_celestial_step", new SkillModel(
                "CELESTIAL_STEP", null, "Pas Céleste", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_STEP"), List.of("WIND_STEP", "GALE_STEP", "STORM_STEP", "VOID_STEP", "DIMENSIONAL_STEP")
        ));
        register("skill_page_omniscient_precision", new SkillModel(
                "OMNISCIENT_PRECISION", null, "Précision Omnisciente", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_PRECISION"), List.of("KEEN_SENSES", "RAZOR_SENSES", "DEADLY_PRECISION", "FATAL_PRECISION", "ABSOLUTE_PRECISION")
        ));

        // --- LÉGENDAIRE DETECTION ---
        register("skill_page_genesis_vision", new SkillModel(
                "GENESIS_VISION", null, "Vision de la Genèse", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_VISION"), List.of("EAGLE_EYE", "HAWK_EYE", "EAGLE_VISION", "OMNISCIENT_VISION", "ALL_SEEING_EYE")
        ));
        register("skill_page_omni_sight", new SkillModel(
                "OMNI_SIGHT", null, "Vision Totale", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ABSOLUTE_SIGHT"), List.of("SIXTH_SENSE", "PSYCHIC_AWARENESS", "MIND_READER", "SOUL_READER", "TRUE_SIGHT")
        ));

        // --- LÉGENDAIRE ENDURANCE ---
        register("skill_page_celestial_lungs", new SkillModel(
                "CELESTIAL_LUNGS", null, "Poumons Célestes", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_LUNGS"), List.of("TIRELESS_BREATH", "LUNGS_OF_STEEL", "DRAGON_LUNGS", "TITAN_LUNGS", "COSMIC_LUNGS")
        ));
        register("skill_page_celestial_fortress", new SkillModel(
                "CELESTIAL_FORTRESS", null, "Forteresse Céleste", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_FORTRESS"), List.of("SOLID_STANCE", "UNMOVABLE_MOUNTAIN", "LIVING_FORTRESS", "ETERNAL_FORTRESS", "DIVINE_FORTRESS")
        ));

        // --- LÉGENDAIRE MAGIQUE ---
        register("skill_page_infinite_mind", new SkillModel(
                "INFINITE_MIND", null, "Esprit Infini", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_MIND"), List.of("AWAKENED_MIND", "EXPANDED_MIND", "BRILLIANT_MIND", "GENIUS_MIND", "COSMIC_MIND")
        ));
        register("skill_page_arcane_genesis", new SkillModel(
                "ARCANE_GENESIS", null, "Genèse Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_CREATION"), List.of("ARCANE_STRIKE", "SPELLBLADE", "ARCANE_DEVASTATION", "ARCANE_ANNIHILATION", "ARCANE_OBLIVION")
        ));

        // --- LÉGENDAIRE CHANCE ---
        register("skill_page_genesis_strike", new SkillModel(
                "GENESIS_STRIKE", null, "Frappe de la Genèse", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_STRIKE"), List.of("LUCKY_STRIKE", "CRITICAL_LUCK", "DESTINY_STRIKE", "DIVINE_STRIKE", "COSMIC_STRIKE")
        ));
        register("skill_page_time_dodge", new SkillModel(
                "TIME_DODGE", null, "Esquive Temporelle", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FATE_DODGE"), List.of("MIRACLE_DODGE", "PHANTOM_DODGE", "SHADOW_DODGE", "DIMENSIONAL_DODGE", "REALITY_DODGE")
        ));

        // --- LÉGENDAIRE REGENERATION ---
        register("skill_page_genesis_blood", new SkillModel(
                "GENESIS_BLOOD", null, "Sang de la Genèse", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_BLOOD"), List.of("CELLULAR_REGENERATION", "TROLL_BLOOD", "HYDRA_BLOOD", "PHOENIX_BLOOD", "ETERNAL_BLOOD")
        ));
        register("skill_page_infinite_vampirism", new SkillModel(
                "INFINITE_VAMPIRISM", null, "Vampirisme Infini", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_VAMPIRISM"), List.of("SPIRITUAL_SIPHON", "SOUL_STEALER", "SPIRIT_DRAIN", "ARCANE_VAMPIRISM", "COSMIC_VAMPIRISM")
        ));

        // --- LÉGENDAIRE RÉSISTANCE ---
        register("skill_page_celestial_immortality", new SkillModel(
                "CELESTIAL_IMMORTALITY", null, "Immortalité Céleste", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TRUE_IMMORTALITY"), List.of("TENACITY", "UNYIELDING", "UNBREAKABLE", "INVINCIBLE", "IMMORTAL_ABSOLUTE")
        ));
        register("skill_page_genesis_bulwark", new SkillModel(
                "GENESIS_BULWARK", null, "Rempart de la Genèse", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_BULWARK"), List.of("ADAPTIVE_SHIELD", "REACTIVE_BULWARK", "ADAMANTINE_BULWARK", "DIVINE_BULWARK", "COSMIC_BULWARK")
        ));

        // --- LÉGENDAIRE VIE ---
        register("skill_page_celestial_constitution", new SkillModel(
                "CELESTIAL_CONSTITUTION", null, "Constitution Céleste", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_CONSTITUTION"), List.of("ROBUST_CONSTITUTION", "STEEL_CONSTITUTION", "TITAN_CONSTITUTION", "GOD_CONSTITUTION", "COSMIC_CONSTITUTION")
        ));
        register("skill_page_heart_of_genesis", new SkillModel(
                "HEART_OF_GENESIS", null, "Coeur de la Genèse", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("HEART_OF_CREATION"), List.of("HEART_OF_OAK", "HEART_OF_IRON", "HEART_OF_STEEL", "HEART_OF_DIAMOND", "HEART_OF_ETERNITY")
        ));

        // --- LÉGENDAIRE MAÎTRISE ---
        register("skill_page_divin_axe_mastery", new SkillModel(
                "DIVIN_AXE_MASTERY", null, "Maîtrise Divine de la Hache", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_AXE_MASTERY"), List.of("AXE_MASTERY", "GREAT_AXE_MASTERY", "WAR_AXE_MASTERY", "LEGENDARY_AXE_MASTERY")
        ));
        register("skill_page_divin_dagger_mastery", new SkillModel(
                "DIVIN_DAGGER_MASTERY", null, "Maîtrise Divine de la Dague", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("CREATOR_DAGGER_MASTERY"), List.of("DAGGER_MASTERY", "SHADOW_BLADE_MASTERY", "ASSASSIN_BLADE_MASTERY", "LEGENDARY_DAGGER_MASTERY")
        ));

        // =============================================
        // === DIVIN SKILLS (Évolutions Divines) ===
        // =============================================

        // --- DIVIN ATTACK ---
        register("skill_page_creator_edge", new SkillModel(
                "CREATOR_EDGE", null, "Tranchant du Créateur", "warrior, assassin, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("DEEP_SLASH", "SHARP_BLADE", "CRIMSON_BLADE", "VOID_BLADE", "ABYSS_BLADE", "GENESIS_EDGE")
        ));
        register("skill_page_creator_swiftness", new SkillModel(
                "CREATOR_SWIFTNESS", null, "Vivacité du Créateur", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("DUELIST_SWIFTNESS", "COMBATANT_SWIFTNESS", "WARRIOR_SWIFTNESS", "BERSERKER_SWIFTNESS", "DEMIGOD_SWIFTNESS", "GOD_SLAYER_SWIFTNESS")
        ));

        // --- DIVIN DEFENSE ---
        register("skill_page_absolute_resolve", new SkillModel(
                "ABSOLUTE_RESOLVE", null, "Résolution Absolue", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("IRON_RESOLVE", "STEEL_RESOLVE", "TITAN_RESOLVE", "GOD_RESOLVE", "IMMORTAL_RESOLVE", "ETERNITY_RESOLVE")
        ));
        register("skill_page_creator_parry", new SkillModel(
                "CREATOR_PARRY", null, "Parade du Créateur", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("MINOR_PARRY", "EXPERT_PARRY", "MASTER_PARRY", "PERFECT_PARRY", "DIVINE_PARRY", "CELESTIAL_PARRY")
        ));

        // --- DIVIN AGILITÉ ---
        register("skill_page_creator_step", new SkillModel(
                "CREATOR_STEP", null, "Pas du Créateur", "novice, warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("WIND_STEP", "GALE_STEP", "STORM_STEP", "VOID_STEP", "DIMENSIONAL_STEP", "CELESTIAL_STEP")
        ));
        register("skill_page_creator_precision", new SkillModel(
                "CREATOR_PRECISION", null, "Précision du Créateur", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("KEEN_SENSES", "RAZOR_SENSES", "DEADLY_PRECISION", "FATAL_PRECISION", "ABSOLUTE_PRECISION", "OMNISCIENT_PRECISION")
        ));

        // --- DIVIN DETECTION ---
        register("skill_page_creator_vision", new SkillModel(
                "CREATOR_VISION", null, "Vision du Créateur", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("EAGLE_EYE", "HAWK_EYE", "EAGLE_VISION", "OMNISCIENT_VISION", "ALL_SEEING_EYE", "GENESIS_VISION")
        ));
        register("skill_page_absolute_sight", new SkillModel(
                "ABSOLUTE_SIGHT", null, "Vue Absolue", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SIXTH_SENSE", "PSYCHIC_AWARENESS", "MIND_READER", "SOUL_READER", "TRUE_SIGHT", "OMNI_SIGHT")
        ));

        // --- DIVIN ENDURANCE ---
        register("skill_page_creator_lungs", new SkillModel(
                "CREATOR_LUNGS", null, "Poumons du Créateur", "warrior, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("TIRELESS_BREATH", "LUNGS_OF_STEEL", "DRAGON_LUNGS", "TITAN_LUNGS", "COSMIC_LUNGS", "CELESTIAL_LUNGS")
        ));
        register("skill_page_creator_fortress", new SkillModel(
                "CREATOR_FORTRESS", null, "Forteresse du Créateur", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SOLID_STANCE", "UNMOVABLE_MOUNTAIN", "LIVING_FORTRESS", "ETERNAL_FORTRESS", "DIVINE_FORTRESS", "CELESTIAL_FORTRESS")
        ));

        // --- DIVIN MAGIQUE ---
        register("skill_page_creator_mind", new SkillModel(
                "CREATOR_MIND", null, "Esprit du Créateur", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("AWAKENED_MIND", "EXPANDED_MIND", "BRILLIANT_MIND", "GENIUS_MIND", "COSMIC_MIND", "INFINITE_MIND")
        ));
        register("skill_page_arcane_creation", new SkillModel(
                "ARCANE_CREATION", null, "Création Arcanique", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("ARCANE_STRIKE", "SPELLBLADE", "ARCANE_DEVASTATION", "ARCANE_ANNIHILATION", "ARCANE_OBLIVION", "ARCANE_GENESIS")
        ));

        // --- DIVIN CHANCE ---
        register("skill_page_creator_strike", new SkillModel(
                "CREATOR_STRIKE", null, "Frappe du Créateur", "merchant, assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("LUCKY_STRIKE", "CRITICAL_LUCK", "DESTINY_STRIKE", "DIVINE_STRIKE", "COSMIC_STRIKE", "GENESIS_STRIKE")
        ));
        register("skill_page_fate_dodge", new SkillModel(
                "FATE_DODGE", null, "Esquive du Destin", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("MIRACLE_DODGE", "PHANTOM_DODGE", "SHADOW_DODGE", "DIMENSIONAL_DODGE", "REALITY_DODGE", "TIME_DODGE")
        ));

        // --- DIVIN REGENERATION ---
        register("skill_page_creator_blood", new SkillModel(
                "CREATOR_BLOOD", null, "Sang du Créateur", "warrior, mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("CELLULAR_REGENERATION", "TROLL_BLOOD", "HYDRA_BLOOD", "PHOENIX_BLOOD", "ETERNAL_BLOOD", "GENESIS_BLOOD")
        ));
        register("skill_page_creator_vampirism", new SkillModel(
                "CREATOR_VAMPIRISM", null, "Vampirisme du Créateur", "mage, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SPIRITUAL_SIPHON", "SOUL_STEALER", "SPIRIT_DRAIN", "ARCANE_VAMPIRISM", "COSMIC_VAMPIRISM", "INFINITE_VAMPIRISM")
        ));

        // --- DIVIN RÉSISTANCE ---
        register("skill_page_true_immortality", new SkillModel(
                "TRUE_IMMORTALITY", null, "Immortalité Véritable", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("TENACITY", "UNYIELDING", "UNBREAKABLE", "INVINCIBLE", "IMMORTAL_ABSOLUTE", "CELESTIAL_IMMORTALITY")
        ));
        register("skill_page_creator_bulwark", new SkillModel(
                "CREATOR_BULWARK", null, "Rempart du Créateur", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("ADAPTIVE_SHIELD", "REACTIVE_BULWARK", "ADAMANTINE_BULWARK", "DIVINE_BULWARK", "COSMIC_BULWARK", "GENESIS_BULWARK")
        ));

        // --- DIVIN VIE ---
        register("skill_page_creator_constitution", new SkillModel(
                "CREATOR_CONSTITUTION", null, "Constitution du Créateur", "warrior, mage, assassin, archer, merchant, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("ROBUST_CONSTITUTION", "STEEL_CONSTITUTION", "TITAN_CONSTITUTION", "GOD_CONSTITUTION", "COSMIC_CONSTITUTION", "CELESTIAL_CONSTITUTION")
        ));
        register("skill_page_heart_of_creation", new SkillModel(
                "HEART_OF_CREATION", null, "Coeur de la Création", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("HEART_OF_OAK", "HEART_OF_IRON", "HEART_OF_STEEL", "HEART_OF_DIAMOND", "HEART_OF_ETERNITY", "HEART_OF_GENESIS")
        ));

        // --- DIVIN MAÎTRISE ---
        register("skill_page_creator_axe_mastery", new SkillModel(
                "CREATOR_AXE_MASTERY", null, "Maîtrise Absolue de la Hache", "warrior, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("AXE_MASTERY", "GREAT_AXE_MASTERY", "WAR_AXE_MASTERY", "LEGENDARY_AXE_MASTERY", "DIVIN_AXE_MASTERY")
        ));
        register("skill_page_creator_dagger_mastery", new SkillModel(
                "CREATOR_DAGGER_MASTERY", null, "Maîtrise Absolue de la Dague", "assassin, archer, dragon",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("DAGGER_MASTERY", "SHADOW_BLADE_MASTERY", "ASSASSIN_BLADE_MASTERY", "LEGENDARY_DAGGER_MASTERY", "DIVIN_DAGGER_MASTERY")
        ));

        // --- COMPÉTENCES ACTIVES (SORTS) ---
        register("skill_page_boule_de_feu", new SkillModel(
                "BOULE_DE_FEU", "Spell_BouleDeFeu", "Boule de Feu", "mage, dragon",
                100, 0.0f, 1.0f, 5.0f, 30.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_flamme_ardente", new SkillModel(
                "FLAMME_ARDENTE", "Spell_FlammeArdente", "Flamme Ardente", "mage, dragon",
                150, 0.0f, 1.0f, 3.0f, 30.0f, 3.0f,
                List.of(), List.of()
        ));
        register("skill_page_souffle_embrase", new SkillModel(
                "SOUFFLE_EMBRASE", "Spell_SouffleEmbrase", "Souffle Embrasé", "mage, dragon",
                300, 0.0f, 1.2f, 8.0f, 5.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_piege_incendiaire", new SkillModel(
                "PIEGE_INCENDIAIRE", "Spell_PiegeIncendiaire", "Piège Incendiaire", "mage, dragon",
                300, 0.0f, 1.0f, 2.0f, 15.0f, 5.0f,
                List.of(), List.of()
        ));
        register("skill_page_meteore", new SkillModel(
                "METEORE", "Spell_Meteore", "Météore", "mage, dragon",
                2500, 0.0f, 1.5f, 20.0f, 30.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_nova_de_feu", new SkillModel(
                "NOVA_DE_FEU", "Spell_NovaDeFeu", "Nova de Feu", "mage, dragon",
                1200, 0.0f, 1.5f, 15.0f, 5.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_inferno", new SkillModel(
                "INFERNO", "Spell_Inferno", "Inferno", "mage, dragon",
                600, 0.0f, 1.5f, 10.0f, 30.0f, 5.0f,
                List.of(), List.of()
        ));
        register("skill_page_souffle_du_dragon", new SkillModel(
                "SOUFFLE_DU_DRAGON", "Spell_SouffleDuDragon", "Souffle du Dragon", "mage, dragon",
                2500, 0.0f, 2.0f, 25.0f, 8.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_apocalypse_ignee", new SkillModel(
                "APOCALYPSE_IGNEE", "Spell_ApocalypseIgnee", "Apocalypse Ignée", "mage, dragon",
                4000, 0.0f, 2.5f, 50.0f, 30.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_baton_magique", new SkillModel(
                "BATON_MAGIQUE", "Weapon_Staff_Student", "Bâton Magique", "mage",
                50, 0.0f, 0.0f, 15.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_tempete_elementaire", new SkillModel(
                "TEMPETE_ELEMENTAIRE", "Spell_TempeteElementaire", "Tempête Élémentaire", "mage, elementaliste",
                300, 0.0f, 1.5f, 25.0f, 10.0f, 5.0f,
                List.of(), List.of()
        ));
        register("skill_page_lien_enchantement", new SkillModel(
                "LIEN_ENCHANTEMENT", "Spell_LienEnchantement", "Lien d'Enchantement", "mage, enchanteur",
                200, 0.0f, 1.2f, 0.0f, 0.0f, 10.0f,
                List.of(), List.of()
        ));
        register("skill_page_benediction_arcanique", new SkillModel(
                "BENEDICTION_ARCANIQUE", "Spell_BenedictionArcanique", "Bénédiction Arcanique", "mage, enchanteur",
                250, 0.0f, 1.2f, 0.0f, 0.0f, 12.0f,
                List.of(), List.of()
        ));
        register("skill_page_bouclier_mystique", new SkillModel(
                "BOUCLIER_MYSTIQUE", "Spell_BouclierMystique", "Bouclier Mystique", "mage, enchanteur",
                350, 0.0f, 1.5f, 0.0f, 0.0f, 15.0f,
                List.of(), List.of()
        ));
        register("skill_page_aura_de_puissance", new SkillModel(
                "AURA_DE_PUISSANCE", "Spell_AuraDePuissance", "Aura de Puissance", "mage, enchanteur",
                500, 0.0f, 1.8f, 0.0f, 8.0f, 8.0f,
                List.of(), List.of()
        ));
        register("skill_page_main_du_trepas", new SkillModel(
                "MAIN_DU_TREPAS", "Spell_MainDuTrepas", "Main du Trépas", "mage, necromancien",
                250, 0.0f, 1.5f, 20.0f, 30.0f, 6.0f,
                List.of(), List.of()
        ));
        register("skill_page_eclat_de_glace", new SkillModel(
                "ECLAT_DE_GLACE", "Spell_EclatDeGlace", "Éclat de Glace", "mage, cryomancien",
                80, 0.0f, 1.0f, 18.0f, 30.0f, 6.0f,
                List.of(), List.of()
        ));
        register("skill_page_drain_vital", new SkillModel(
                "DRAIN_VITAL", "Spell_DrainVital", "Drain Vital", "mage, necromancien",
                200, 0.0f, 1.5f, 18.0f, 25.0f, 12.0f,
                List.of(), List.of()
        ));
        register("skill_page_appel_des_morts", new SkillModel(
                "APPEL_DES_MORTS", "Spell_AppelDesMorts", "Appel des Morts", "mage, necromancien",
                350, 0.0f, 2.0f, 6.0f, 15.0f, 8.0f,
                List.of(), List.of()
        ));
        register("skill_page_malediction_necrotique", new SkillModel(
                "MALEDICTION_NECROTIQUE", "Spell_MaledictionNecrotique", "Malédiction Nécrotique", "mage, necromancien",
                300, 0.0f, 1.8f, 10.0f, 20.0f, 3.0f,
                List.of(), List.of()
        ));
        register("skill_page_flamme_elementaire", new SkillModel(
                "FLAMME_ELEMENTAIRE", "Spell_FlammeElementaire", "Flamme Élémentaire", "mage, elementaliste",
                120, 0.0f, 1.0f, 8.0f, 30.0f, 4.0f,
                List.of(), List.of()
        ));
        register("skill_page_eclat_glacial", new SkillModel(
                "ECLAT_GLACIAL", "Spell_EclatGlacial", "Éclat Glacial", "mage, elementaliste",
                200, 0.0f, 1.2f, 12.0f, 8.0f, 5.0f,
                List.of(), List.of()
        ));
        register("skill_page_arc_foudroyant", new SkillModel(
                "ARC_FOUDROYANT", "Spell_ArcFoudroyant", "Arc Foudroyant", "mage, elementaliste",
                180, 0.0f, 0.8f, 22.0f, 30.0f, 0.0f,
                List.of(), List.of()
        ));

        // --- CHRONOMANCIEN ---
        register("skill_page_distorsion_temporelle", new SkillModel(
                "DISTORSION_TEMPORELLE", "Spell_DistorsionTemporelle", "Distorsion Temporelle", "mage, chronomancien",
                250, 0.0f, 1.4f, 0.0f, 8.0f, 6.0f,
                List.of(), List.of()
        ));
        register("skill_page_acceleration_chrono", new SkillModel(
                "ACCELERATION_CHRONO", "Spell_AccelerationChrono", "Acceleration Chrono", "mage, chronomancien",
                180, 0.0f, 1.0f, 0.0f, 6.0f, 8.0f,
                List.of(), List.of()
        ));
        register("skill_page_rembobinage", new SkillModel(
                "REMBOBINAGE", "Spell_Rembobinage", "Rembobinage", "mage, chronomancien",
                400, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        // --- ILLUSIONNISTE ---
        register("skill_page_clone_illusoire", new SkillModel(
                "CLONE_ILLUSOIRE", "Spell_CloneIllusoire", "Clone Illusoire", "mage, illusionniste",
                180, 0.0f, 1.2f, 0.0f, 0.0f, 8.0f,
                List.of(), List.of()
        ));
        register("skill_page_voile_d_ombre", new SkillModel(
                "VOILE_D_OMBRE", "Spell_VoileDOmbre", "Voile d'Ombre", "mage, illusionniste",
                250, 0.0f, 1.5f, 0.0f, 0.0f, 7.0f,
                List.of(), List.of()
        ));
        register("skill_page_confusion_arcanique", new SkillModel(
                "CONFUSION_ARCANIQUE", "Spell_ConfusionArcanique", "Confusion Arcanique", "mage, illusionniste",
                300, 0.0f, 1.0f, 8.0f, 15.0f, 5.0f,
                List.of(), List.of()
        ));

        // --- GUERISSEUR ---
        register("skill_page_lumiere_guerisseuse", new SkillModel(
                "LUMIERE_GUERISSEUSE", "Spell_LumiereGuerisseuse", "Lumiere Guerisseuse", "mage, guerisseur",
                200, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));
        register("skill_page_purification_sacree", new SkillModel(
                "PURIFICATION_SACREE", "Spell_PurificationSacree", "Purification Sacree", "mage, guerisseur",
                350, 0.0f, 1.5f, 0.0f, 6.0f, 3.0f,
                List.of(), List.of()
        ));
        register("skill_page_souffle_de_vie", new SkillModel(
                "SOUFFLE_DE_VIE", "Spell_SouffleDeVie", "Souffle de Vie", "mage, guerisseur",
                600, 0.0f, 2.5f, 0.0f, 8.0f, 5.0f,
                List.of(), List.of()
        ));

        // --- INVOCATEUR ---
        register("skill_page_familier_arcanique", new SkillModel(
                "FAMILIER_ARCANIQUE", "Spell_FamilierArcanique", "Familier Arcanique", "mage, invocateur",
                250, 0.0f, 1.2f, 6.0f, 4.0f, 12.0f,
                List.of(), List.of()
        ));
        register("skill_page_totem_de_garde", new SkillModel(
                "TOTEM_DE_GARDE", "Spell_TotemDeGarde", "Totem de Garde", "mage, invocateur",
                300, 0.0f, 1.5f, 0.0f, 5.0f, 10.0f,
                List.of(), List.of()
        ));
        register("skill_page_portail_dimensionnel", new SkillModel(
                "PORTAIL_DIMENSIONNEL", "Spell_PortailDimensionnel", "Portail Dimensionnel", "mage, invocateur",
                400, 0.0f, 1.0f, 0.0f, 0.0f, 6.0f,
                List.of(), List.of()
        ));
    }

    private static void register(String itemId, SkillModel skill) {
        ITEM_TO_SKILL.put(itemId, skill);
        ID_TO_SKILL.put(skill.skillId(), skill);
    }

    public static Optional<SkillModel> getSkillFromItem(String itemId) {
        return Optional.ofNullable(ITEM_TO_SKILL.get(itemId));
    }

    public static Optional<SkillModel> getSkillFromId(String skillId) {
        return Optional.ofNullable(ID_TO_SKILL.get(skillId));
    }

    public static Collection<SkillModel> getAllSkills() {
        return ID_TO_SKILL.values();
    }
}