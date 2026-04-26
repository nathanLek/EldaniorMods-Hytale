package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Faucheur extends ClassModel {
    public Faucheur() {
        super("faucheur", "Faucheur", "Le Faucheur est la mort incarnee. Ses lames moissonnent les vies comme le ble sous la faux.",
                Rarity.EPIC, ClassType.ASSASSIN,
                List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.BLOOD_HUNT, PassiveSkill.LIGHTNING_REFLEXES),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("faucheur_eternel", "moissonneur_d_ames", "ange_noir"), 400, false,
                80, 30, 5, 30, 90, 60);
    }
}
