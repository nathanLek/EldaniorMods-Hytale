package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SicaireElite extends ClassModel {
    public SicaireElite() {
        super("sicaire_elite", "Sicaire Elite", "Un tueur d'elite dont chaque frappe est calculee pour tuer.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.RAZOR_SENSES, PassiveSkill.PRESSURE_POINT, PassiveSkill.PREDATORY_STRIKE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                18, 7, 4, 4, 30, 20);
    }
}
