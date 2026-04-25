package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Spectre extends ClassModel {
    public Spectre() {
        super("spectre_assassin", "Spectre", "Le Spectre existe entre le monde des vivants et celui des morts. Ses attaques sont impossibles a esquiver.",
                Rarity.UNIQUE, ClassType.ASSASSIN,
                List.of(PassiveSkill.VOID_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.DIMENSIONAL_DODGE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                80, 50, 15, 40, 160, 120);
    }
}
