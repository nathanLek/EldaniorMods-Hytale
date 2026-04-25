package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameEternelle extends ClassModel {
    public LameEternelle() {
        super("lame_eternelle", "Lame Eternelle", "La Lame Eternelle a transcende les limites mortelles. Chaque coup porte la certitude de la mort.",
                Rarity.UNIQUE, ClassType.ASSASSIN,
                List.of(PassiveSkill.VOID_BLADE, PassiveSkill.BERSERKER_SWIFTNESS, PassiveSkill.ABSOLUTE_PRECISION),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                100, 40, 10, 40, 180, 100);
    }
}
