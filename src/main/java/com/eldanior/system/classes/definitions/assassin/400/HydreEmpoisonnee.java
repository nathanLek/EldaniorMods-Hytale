package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class HydreEmpoisonnee extends ClassModel {
    public HydreEmpoisonnee() {
        super("hydre_empoisonnee", "Hydre Empoisonnee", "Comme l'hydre, chaque blessure le rend plus dangereux.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.HYDRA_BLOOD, PassiveSkill.HAUNTING_THRUST, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                72, 64, 48, 32, 134, 152);
    }
}
