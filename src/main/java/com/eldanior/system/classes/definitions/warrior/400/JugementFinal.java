package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class JugementFinal extends ClassModel {
    public JugementFinal() {
        super("jugement_final", "Jugement Final", "Le Jugement Final prononce la sentence de mort irrevocable. Sa lame d'abime et sa traque implacable sont le dernier verdict que subissent ses victimes.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.SOUL_CRUSHING_PRESSURE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                344, 130, 17, 134, 166, 150);
    }
}