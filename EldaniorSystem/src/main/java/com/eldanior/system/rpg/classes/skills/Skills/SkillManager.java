package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.rpg.classes.skills.Skills.definitions.AuraDragonicDivin;
import com.eldanior.system.rpg.classes.skills.system.config.AuraDragonicConfig; // Import de la config
import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private static final Map<String, SkillModel> skills = new HashMap<>();

    public static void init() {
        // Initialisation de la config (par défaut ici, ou chargée via un fichier)
        AuraDragonicConfig dragonConfig = new AuraDragonicConfig();

        // Enregistrement avec la config
        register(new AuraDragonicDivin(dragonConfig));

        System.out.println("[Eldanior] Skills Passifs charges.");
    }

    public static void register(SkillModel skill) {
        skills.put(skill.getId(), skill);
    }

    public static SkillModel get(String id) {
        return skills.get(id);
    }
}