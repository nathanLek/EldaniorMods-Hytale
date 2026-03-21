package com.eldanior.system.skills;

import com.eldanior.system.skills.models.SkillModel;

import java.util.*;

public class SkillManager {

    private static final Map<String, SkillModel> ITEM_TO_SKILL = new HashMap<>();
    private static final Map<String, SkillModel> ID_TO_SKILL = new HashMap<>();

    public static void init() {

        // --- ATTAQUE ---
        register("skill_page_mauvais_presage", new SkillModel(
                "MAUVAIS_PRESAGE", null, "Mauvais Présage", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_koda_judgment", new SkillModel(
                "KODA_JUDGMENT", null, "Jugement de Koda", "warrior", // Adapté pour le warrior ou l'assassin
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), // Légendaire, donc pas d'évolution suivante pour le moment !
                List.of()
        ));
        register("skill_page_opportunist_strike", new SkillModel(
                "OPPORTUNIST_STRIKE", null, "Frappe Opportuniste", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_haunting_thrust", new SkillModel(
                "HAUNTING_THRUST", null, "Estocade Obsédante", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_pressure_point", new SkillModel(
                "PRESSURE_POINT", null, "Point de Pression", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_duelist_swiftness", new SkillModel(
                "DUELIST_SWIFTNESS", null, "Vivacité du Duelliste", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_deep_slash", new SkillModel(
                "DEEP_SLASH", null, "Entaille Profonde", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));

        register("skill_page_instinctive_strike", new SkillModel(
                "INSTINCTIVE_STRIKE", null, "Frappe Instinctive", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PREDATORY_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of()
        ));
        register("skill_page_predatory_strike", new SkillModel(
                "PREDATORY_STRIKE",
                null, // Ici, on met l'ID du skill précédent si c'est une évolution
                "Frappe de Prédateur",
                "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("FURY_STRIKE","SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("INSTINCTIVE_STRIKE")
        ));
        register("skill_page_fury_strike", new SkillModel(
                "FURY_STRIKE",
                "null", // Ici, on met l'ID du skill précédent si c'est une évolution
                "Frappe de Fureur",
                "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("PREDATORY_STRIKE", "INSTINCTIVE_STRIKE")
        ));
        register("skill_page_seismic_strike", new SkillModel(
                "SEISMIC_STRIKE",
                null,
                "Frappe Sismique", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("PHANTOM_STRIKE", "ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("PREDATORY_STRIKE", "INSTINCTIVE_STRIKE", "FURY_STRIKE")
        ));
        register("skill_page_phantom_strike", new SkillModel(
                "PHANTOM_STRIKE",
                null,
                "Frappe Fantôme", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ANNIHILATOR_STRIKE", "JUDGMENT_OF_GENESIS"),
                List.of("PREDATORY_STRIKE", "INSTINCTIVE_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE" )
        ));
        register("skill_page_annihilator_strike", new SkillModel(
                "ANNIHILATOR_STRIKE",
                null,
                "Frappe de l'Annihilateur", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("JUDGMENT_OF_GENESIS"),
                List.of("PREDATORY_STRIKE", "INSTINCTIVE_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE")
        ));
        register("skill_page_judgment_of_genesis", new SkillModel(
                "JUDGMENT_OF_GENESIS",
                null,
                "Décret de la Genèse", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of("PREDATORY_STRIKE", "INSTINCTIVE_STRIKE", "FURY_STRIKE", "SEISMIC_STRIKE", "PHANTOM_STRIKE", "ANNIHILATOR_STRIKE")
        ));

        // --- DEFENSE ---
        register("skill_page_dyna_aegis", new SkillModel(
                "DYNA_AEGIS", null, "Égide de Dyna", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_iron_resolve", new SkillModel(
                "IRON_RESOLVE", null, "Résolution de Fer", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STEEL_RESOLVE", "TITAN_RESOLVE", "UNBREAKABLE_WILL"), // Évolutions futures
                List.of()
        ));
        register("skill_page_minor_parry", new SkillModel(
                "MINOR_PARRY", null, "Parade Mineure", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("EXPERT_PARRY", "MASTER_PARRY", "PERFECT_RIPOSTE"), // Évolutions futures
                List.of()
        ));
        register("skill_page_hunter_guard", new SkillModel(
                "HUNTER_GUARD", null, "Garde de Chasseur", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BEAST_GUARD", "MONSTER_HUNTER_GUARD", "DRAGON_SLAYER_GUARD"), // Évolutions futures
                List.of()
        ));
        register("skill_page_sturdy_body", new SkillModel(
                "STURDY_BODY", null, "Corps Robuste", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_BODY", "STEEL_BODY", "DIAMOND_BODY", "IMMORTAL_BODY"), // Évolutions futures
                List.of()
        ));
        register("skill_page_stone_skin", new SkillModel(
                "STONE_SKIN", null, "Peau de Pierre", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of()
        ));
        register("skill_page_bronze_skin", new SkillModel(
                "BRONZE_SKIN", null, "Peau de Bronze", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN")
        ));
        register("skill_page_iron_skin", new SkillModel(
                "IRON_SKIN", null, "Peau de Fer", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN")
        ));
        register("skill_page_steel_skin", new SkillModel(
                "STEEL_SKIN", null, "Peau d'Acier", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("OBSIDIAN_SKIN", "DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN")
        ));
        register("skill_page_obsidian_skin", new SkillModel(
                "OBSIDIAN_SKIN", null, "Peau d'Obsidienne", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIAMOND_SKIN", "DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN")
        ));
        register("skill_page_diamond_skin", new SkillModel(
                "DIAMOND_SKIN", null, "Peau de Diamant", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("DIVINE_AEGIS"),
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN")
        ));
        register("skill_page_divine_aegis", new SkillModel(
                "DIVINE_AEGIS", null, "Égide Divine", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), // Puissance maximale atteinte !
                List.of("STONE_SKIN", "BRONZE_SKIN", "IRON_SKIN", "STEEL_SKIN", "OBSIDIAN_SKIN", "DIAMOND_SKIN")
        ));

        // --- AGILITY ---
        register("skill_page_wind_step", new SkillModel(
                "WIND_STEP", null, "Pas de Vent", "novice, warrior, assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_light_reflexes", new SkillModel(
                "LIGHT_REFLEXES", null, "Réflexes Éclairs", "warrior, assassin, novice",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_suppleness", new SkillModel(
                "ELDANIOR_SUPPLENESS", null, "Souplesse d'Eldanior", "novice, warrior, assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_athleticism", new SkillModel(
                "ATHLETICISM", null, "Athlétisme", "novice, warrior, assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_eagle_eye", new SkillModel(
                "EAGLE_EYE", null, "Œil de Rapace", "warrior, assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        register("skill_page_tono_squive", new SkillModel(
                "TONOSQUIVE", null, "Esquive de Tono", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        // --- DETECTION ---
        register("skill_page_survival_instinct", new SkillModel(
                "SURVIVAL_INSTINCT", null, "Instinct de Survie", "novice, warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNIVERSAL_DETECTION"), List.of()
        ));
        register("skill_page_keen_senses", new SkillModel(
                "KEEN_SENSES", null, "Sens Aiguisés", "assassin, warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_universal_detection", new SkillModel(
                "UNIVERSAL_DETECTION", null, "Detection Universel", "mage, warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of("SURVIVAL_INSTINCT")
        ));

        register("skill_page_night_vision", new SkillModel(
                "NIGHT_VISION", null, "Vision Nocturne", "warrior, assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        register("skill_page_sixth_sense", new SkillModel(
                "SIXTH_SENSE", null, "Sixième Sens", "warrior, assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        register("skill_page_tracker", new SkillModel(
                "TRACKER", null, "Pisteur", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), List.of()
        ));

        // --- MAITRISE ---
        register("skill_page_sword_mastery", new SkillModel(
                "SWORD_MASTERY", null, "Maîtrise de l'Épée", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));

        // --- MAGIE ---
        register("skill_page_mana_well", new SkillModel(
                "MANAWELL", null, "Maîtrise de l'Épée", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_awakened_mind", new SkillModel(
                "AWAKENED_MIND", null, "Esprit Éveillé", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("EXPANDED_MIND"),
                List.of()
        ));

        register("skill_page_arcane_strike", new SkillModel(
                "ARCANE_STRIKE", null, "Frappe Arcanique", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SPELLBLADE"),
                List.of()
        ));

        register("skill_page_mana_barrier", new SkillModel(
                "MANA_BARRIER", null, "Barrière de Mana", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ARCANE_SHIELD"),
                List.of()
        ));

        register("skill_page_overflowing_power", new SkillModel(
                "OVERFLOWING_POWER", null, "Puissance Débordante", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNLEASHED_MAGIC"),
                List.of()
        ));

        register("skill_page_mystic_veil", new SkillModel(
                "MYSTIC_VEIL", null, "Voile Mystique", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("ASTRAL_CLOAK"),
                List.of()
        ));

        // --- CHANCE ---
        register("skill_page_artisanat", new SkillModel(
                "ARTISANAT", null, "Artisanat", "merchant",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_detection_of_vital_points", new SkillModel(
                "DETECTIONOFVITALPOINTS", null, "Maîtrise de l'Épée", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_lucky_strike", new SkillModel(
                "LUCKY_STRIKE", null, "Frappe Chanceuse", "merchant", // J'ai mis merchant, adapte si besoin !
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_treasure_hunter", new SkillModel(
                "TREASURE_HUNTER", null, "Chasseur de Trésors", "merchant",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_good_omen", new SkillModel(
                "GOOD_OMEN", null, "Bon Présage", "merchant",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));

        register("skill_page_miracle_dodge", new SkillModel(
                "MIRACLE_DODGE", null, "Esquive Miraculeuse", "assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));

        // --- ENDURANCE ---
        register("skill_page_tireless_breath", new SkillModel(
                "TIRELESS_BREATH", null, "Souffle Inépuisable", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("LUNGS_OF_STEEL"),
                List.of()
        ));

        register("skill_page_thick_skin", new SkillModel(
                "THICK_SKIN", null, "Peau Épaisse", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("STONE_SKIN"),
                List.of()
        ));

        register("skill_page_solid_stance", new SkillModel(
                "SOLID_STANCE", null, "Posture Solide", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("UNMOVABLE_MOUNTAIN"),
                List.of()
        ));

        register("skill_page_combat_vigor", new SkillModel(
                "COMBAT_VIGOR", null, "Vigueur Combative", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("RELENTLESS_ASSAULT"),
                List.of()
        ));

        register("skill_page_second_wind", new SkillModel(
                "SECOND_WIND", null, "Second Souffle", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SURVIVOR_INSTINCT"),
                List.of()
        ));

        // --- REGENERATION ---
        register("skill_page_mana_font", new SkillModel(
                "MANA_FONT", null, "Source de Mana", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(), // Évolution future possible !
                List.of()
        ));
        register("skill_page_cellular_regeneration", new SkillModel(
                "CELLULAR_REGENERATION", null, "Régénération Cellulaire", "paladin", // Ou "warrior"
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("TROLL_BLOOD"),
                List.of()
        ));
        register("skill_page_active_breathing", new SkillModel(
                "ACTIVE_BREATHING", null, "Respiration Active", "assassin",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("IRON_LUNGS"),
                List.of()
        ));
        register("skill_page_spiritual_siphon", new SkillModel(
                "SPIRITUAL_SIPHON", null, "Siphon Spirituel", "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of("SOUL_STEALER"),
                List.of()
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