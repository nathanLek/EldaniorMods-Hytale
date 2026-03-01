package com.eldanior.system.skills;

import com.eldanior.system.skills.models.SkillModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SkillManager {

    private static final Map<String, SkillModel> ITEM_TO_SKILL = new HashMap<>();
    private static final Map<String, SkillModel> ID_TO_SKILL = new HashMap<>();

    public static void init() {
        register("skill_page_iron_skin", new SkillModel(
                "IRON_SKIN",
                null,
                "Peau de Fer",
                "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        ));
        register("skill_page_athleticism", new SkillModel(
                "ATHLETICISM",
                null,
                "Athlétisme",
                "novice, warrior, assassin", // Accessible à tous par exemple
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        ));
        register("skill_page_sword_mastery", new SkillModel(
                "SWORD_MASTERY",
                null,
                "Maîtrise de l'Épée",
                "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        ));
        register("skill_page_mana_well", new SkillModel(
                "MANAWELL",
                null,
                "Maîtrise de l'Épée",
                "mage",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        ));
        register("skill_page_detection_of_vital_points", new SkillModel(
                "DETECTIONOFVITALPOINTS",
                null,
                "Maîtrise de l'Épée",
                "warrior",
                0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
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