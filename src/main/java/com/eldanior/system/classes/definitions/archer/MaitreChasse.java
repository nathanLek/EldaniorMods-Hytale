package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreChasse extends ClassModel {
    public MaitreChasse() {
        super("maitre_chasse", "Maitre de la Chasse", "Le Maitre de la Chasse connait chaque creature du monde. Aucune proie ne peut lui echapper.",
                Rarity.RARE, ClassType.ARCHER,
                List.of(PassiveSkill.MASTER_TRACKER, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.RAZOR_SENSES),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of(), 250, false,
                20, 20, 5, 15, 55, 45);
    }
}
