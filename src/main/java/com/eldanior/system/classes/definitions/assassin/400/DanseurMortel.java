package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DanseurMortel extends ClassModel {
    public DanseurMortel() {
        super("danseur_mortel", "Danseur Mortel", "Sa danse hypnotise avant de tuer. Chaque mouvement est une lame.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.WIND_STEP, PassiveSkill.LIGHT_REFLEXES, PassiveSkill.SHARP_BLADE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                12, 8, 4, 6, 38, 16);
    }
}
