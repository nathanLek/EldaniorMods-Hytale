package com.eldanior.system.skills;

import com.eldanior.system.skills.models.SkillModel;

import java.util.*;

public class SkillManager {

    private static final Map<String, SkillModel> ITEM_TO_SKILL = new HashMap<>();
    private static final Map<String, SkillModel> ID_TO_SKILL = new HashMap<>();

    public static void init() {
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
                "ATHLETICISM",
                null,
                "Athlétisme",
                "novice, warrior, assassin", // Accessible à tous par exemple
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_sword_mastery", new SkillModel(
                "SWORD_MASTERY",
                null,
                "Maîtrise de l'Épée",
                "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_mana_well", new SkillModel(
                "MANAWELL",
                null,
                "Maîtrise de l'Épée",
                "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                List.of(),
                List.of()
        ));
        register("skill_page_detection_of_vital_points", new SkillModel(
                "DETECTIONOFVITALPOINTS",
                null,
                "Maîtrise de l'Épée",
                "warrior",
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