package com.eldanior.system.skills;

import com.eldanior.system.skills.models.SkillModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SkillManager {
    // On mappe l'ID de l'ITEM (le parchemin) au MODELE du skill
    private static final Map<String, SkillModel> ITEM_TO_SKILL = new HashMap<>();

    public static void init() {
        // Enregistre tes parchemins ici
        register("skill_page_fireball",
                new SkillModel("fireball", "fireball", "Boule de Feu", "mage"));
    }

    private static void register(String itemId, SkillModel skill) {
        ITEM_TO_SKILL.put(itemId, skill);
    }

    public static Optional<SkillModel> getSkillFromItem(String itemId) {
        return Optional.ofNullable(ITEM_TO_SKILL.get(itemId));
    }
}