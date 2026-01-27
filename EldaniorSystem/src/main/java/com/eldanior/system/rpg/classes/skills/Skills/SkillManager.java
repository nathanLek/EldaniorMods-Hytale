package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.rpg.classes.skills.Skills.config.AuraDragonicConfig;
import com.eldanior.system.rpg.classes.skills.Skills.definitions.AuraDragonicDivin;
import com.eldanior.system.rpg.classes.skills.Skills.definitions.FireballSkill;
import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private static final Map<String, SkillModel> skills = new HashMap<>();

    public static void init() {
        // 1. Initialisation des Configs
        AuraDragonicConfig dragonConfig = new AuraDragonicConfig();

        // 2. Enregistrement des Compétences
        // Passifs / Auras
        register(new AuraDragonicDivin(dragonConfig));

        // Actifs / Sorts (C'est ici qu'on ajoute tes nouveaux sorts !)
        register(new FireballSkill());

        System.out.println("[Eldanior] " + skills.size() + " Skills chargés : " + skills.keySet());
    }

    public static void register(SkillModel skill) {
        if (skill != null) {
            skills.put(skill.getId(), skill);
        }
    }

    public static SkillModel get(String id) {
        return skills.get(id);
    }
}