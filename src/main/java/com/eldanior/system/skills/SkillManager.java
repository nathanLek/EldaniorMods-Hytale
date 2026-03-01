package com.eldanior.system.skills;

import com.eldanior.system.skills.models.SkillModel;

import java.util.*;

public class SkillManager {

    private static final Map<String, SkillModel> ITEM_TO_SKILL = new HashMap<>();
    private static final Map<String, SkillModel> ID_TO_SKILL = new HashMap<>();

    public static void init() {
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
        register("skill_page_athleticism", new SkillModel(
                "ATHLETICISM", null, "Athlétisme", "novice, warrior, assassin", // Accessible à tous par exemple
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_sword_mastery", new SkillModel(
                "SWORD_MASTERY", null, "Maîtrise de l'Épée", "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_mana_well", new SkillModel(
                "MANAWELL", null, "Maîtrise de l'Épée", "mage",
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