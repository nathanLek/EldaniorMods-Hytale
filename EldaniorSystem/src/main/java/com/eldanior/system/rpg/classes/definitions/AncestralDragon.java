package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.eldanior.system.rpg.classes.skills.passives.definitions.AuraDragonicDivin;

import java.util.List; // ✅ Import nécessaire

public class AncestralDragon extends ClassModel {

    public AncestralDragon() {
        super(
                "dragon",
                "Dragon Ancestral",
                "Une entite mythique dont les veines bresillent de puissance draconique ancienne.",
                Rarity.DIVINE,
                ClassType.WARRIOR,
                List.of(new AuraDragonicDivin()),
                true,
                null,
                2,
                true,
                10000, 5000, 10000, 800, 400, 5000
        );
    }
}