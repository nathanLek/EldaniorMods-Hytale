package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SaboteurExpert extends ClassModel {
    public SaboteurExpert() {
        super("saboteur_expert", "Saboteur Expert", "Un expert en destruction furtive capable de demanteler n'importe quelle defense.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.STONE_SKIN, PassiveSkill.THUNDER_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                14, 14, 7, 10, 24, 17);
    }
}
