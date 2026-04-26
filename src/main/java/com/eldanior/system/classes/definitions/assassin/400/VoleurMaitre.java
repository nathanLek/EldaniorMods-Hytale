package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VoleurMaitre extends ClassModel {
    public VoleurMaitre() {
        super("voleur_maitre", "Voleur Maitre", "Un maitre du larcin dont les mains sont plus rapides que l'oeil. Nul tresor n'est hors de sa portee.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.RAZOR_SENSES, PassiveSkill.WIND_STEP, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                14, 7, 4, 4, 34, 24);
    }
}
