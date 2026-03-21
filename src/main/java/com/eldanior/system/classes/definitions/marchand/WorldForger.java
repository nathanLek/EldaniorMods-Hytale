package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class WorldForger extends ClassModel {

    public WorldForger() {
        super(
                "world_forger",
                "Forgeron des Mondes",
                "Il ne travaille plus le simple métal, il façonne la réalité elle-même. Son corps est une enclume sur laquelle les armes divines sont forgées.",
                Rarity.LEGENDARY,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                null,
                999,
                false,
                250, 80, 300, 200, 50, 200
        );
    }
}