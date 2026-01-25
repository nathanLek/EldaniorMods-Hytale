package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.rpg.classes.skills.Skills.definitions.AuraDragonicDivin;

import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private static final Map<String, SkillModel> skills = new HashMap<>();

    public static void init() {
        // Enregistrement des compétences concrètes uniquement
        register(new AuraDragonicDivin());

        // Plus tard, tu ajouteras ici : register(new StoneSkin()); etc.

        System.out.println("[Eldanior] Skills Passifs charges.");
    }

    // CORRECTION : Accepte n'importe quel PassiveSkillModel (pas juste AuraDragonicDivin)
    public static void register(SkillModel skill) {
        skills.put(skill.getId(), skill);
    }

    public static SkillModel get(String id) {
        return skills.get(id);
    }
}