package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class Prospecteur extends ClassModel {

    public Prospecteur() {
        super(
                "prospecteur",
                "Prospecteur",
                "Le Prospecteur a un flair incroyable pour dénicher les filons d'or et les tresors caches.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.TREASURE_HUNTER, PassiveSkill.SURVIVAL_INSTINCT),
                List.of(WeaponMastery.ANY),
                List.of("prospecteur_royal"),
                400,
                false,
                4, 6, 4, 6, 8, 22
        );
    }
}