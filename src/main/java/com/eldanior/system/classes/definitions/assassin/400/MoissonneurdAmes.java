package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MoissonneurdAmes extends ClassModel {
    public MoissonneurdAmes() {
        super("moissonneur_d_ames", "Moissonneur d'Ames", "Chaque coup recolte une ame. Son fardeau est eternel.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.CRIMSON_BLADE, PassiveSkill.SPIRIT_DRAIN), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                134, 50, 10, 50, 152, 100);
    }
}
