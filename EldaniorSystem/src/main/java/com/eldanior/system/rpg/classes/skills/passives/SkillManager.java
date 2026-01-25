package com.eldanior.system.rpg.classes.skills.passives;

import com.eldanior.system.rpg.classes.skills.passives.definitions.AuraDragonicDivin;
import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private static final Map<String, PassiveSkillModel> skills = new HashMap<>();

    public static void init() {
        // Enregistrement des compétences concrètes uniquement
        register(new AuraDragonicDivin());

        // Plus tard, tu ajouteras ici : register(new StoneSkin()); etc.

        System.out.println("[Eldanior] Skills Passifs charges.");
    }

    // CORRECTION : Accepte n'importe quel PassiveSkillModel (pas juste AuraDragonicDivin)
    public static void register(PassiveSkillModel skill) {
        skills.put(skill.getId(), skill);
    }

    public static PassiveSkillModel get(String id) {
        return skills.get(id);
    }
}