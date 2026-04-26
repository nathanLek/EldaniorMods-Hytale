package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Franc_Tireur extends ClassModel {
    public Franc_Tireur() {
        super("franc_tireur", "Franc-Tireur", "Le Franc-Tireur frappe depuis des distances impossibles. Quand on entend la fleche, il est deja trop tard.",
                Rarity.RARE, ClassType.ARCHER,
                List.of(PassiveSkill.HAWK_EYE, PassiveSkill.RAZOR_SENSES, PassiveSkill.SHARP_BLADE),
                List.of(WeaponMastery.BOW),
                List.of("tireur_legendaire", "oeil_de_lynx", "sniper_mystique"), 400, false,
                20, 15, 10, 10, 50, 50);
    }
}
