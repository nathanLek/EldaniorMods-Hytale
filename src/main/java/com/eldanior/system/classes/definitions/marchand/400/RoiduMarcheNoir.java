package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RoiduMarcheNoir extends ClassModel {
    public RoiduMarcheNoir() {
        super("roi_du_marche_noir", "Roi du Marche Noir", "Le monarque inconteste du marche noir. Tout a un prix pour lui.",
                Rarity.UNIQUE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.SHADOW_DODGE), List.of(WeaponMastery.ANY), List.of(), 400, false,
                18, 14, 34, 28, 76, 70);
    }
}
