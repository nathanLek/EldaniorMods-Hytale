package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.eldanior.system.rpg.classes.skills.Skills.definitions.AuraDragonicDivin;
import com.eldanior.system.rpg.classes.skills.system.config.AuraDragonicConfig; // ✅ Import de la config

import java.util.List;

public class AncestralDragon extends ClassModel {

    public AncestralDragon() {
        super(
                "dragon",
                "Dragon Ancestral",
                "Une entite mythique dont les veines bresillent de puissance draconique ancienne.",
                Rarity.DIVINE,
                ClassType.DRAGON,
                // ✅ On injecte une nouvelle config ici
                List.of(new AuraDragonicDivin(new AuraDragonicConfig())),
                true,
                null,
                2,
                true,
                200, 200, 10000, 200, 200, 10000
        );
    }
}