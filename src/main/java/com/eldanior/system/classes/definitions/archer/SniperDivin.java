package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SniperDivin extends ClassModel {
    public SniperDivin() {
        super("sniper_divin", "Sniper Divin", "Le Sniper Divin ne rate jamais sa cible. Une fleche, une vie. C'est la regle absolue.",
                Rarity.EPIC, ClassType.ARCHER,
                List.of(PassiveSkill.DEADLY_PRECISION, PassiveSkill.CRIMSON_BLADE, PassiveSkill.EAGLE_VISION),
                List.of(WeaponMastery.BOW),
                List.of("sniper_celeste", "oeil_absolu", "tir_fatal"), 400, false,
                40, 30, 15, 20, 80, 100);
    }
}
