package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class WorldForger extends ClassModel {

    public WorldForger() {
        super(
                "world_forger",
                "Forgeron des Mondes",
                "Le Forgeur de Mondes cree des artefacts si puissants qu'ils peuvent remodeler la realite. Sa forge brule du feu divin.",
                Rarity.LEGENDARY,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.COSMIC_MIND, PassiveSkill.ABYSS_BLADE),
                List.of(WeaponMastery.ANY),
                List.of("forgeur_des_mondes"),
                999,
                false,
                500, 160, 600, 400, 100, 400
        );
    }
}