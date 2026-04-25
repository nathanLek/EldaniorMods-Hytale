package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChevalierNoir extends ClassModel {

    public ChevalierNoir() {
        super(
                "chevalier_noir",
                "Chevalier Noir",
                "Le Chevalier Noir a embrasse les tenebres pour obtenir un pouvoir interdit. Sa puissance corrompue consume tout sur son passage.",
                Rarity.EPIC,
                ClassType.WARRIOR,
                List.of(PassiveSkill.BLOOD_HUNT, PassiveSkill.CRUSHING_PRESSURE, PassiveSkill.STORM_STEP),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                120, 60, 20, 50, 80, 70
        );
    }
}
