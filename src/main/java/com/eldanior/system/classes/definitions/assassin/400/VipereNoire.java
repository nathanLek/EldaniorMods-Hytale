package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VipereNoire extends ClassModel {
    public VipereNoire() {
        super("vipere_noire", "Vipere Noire", "Rapide et venimeuse comme son nom l'indique. Une morsure et c'est fini.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.HAUNTING_THRUST, PassiveSkill.LUCKY_STRIKE, PassiveSkill.PREDATORY_STRIKE), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                12, 6, 8, 2, 30, 24);
    }
}
