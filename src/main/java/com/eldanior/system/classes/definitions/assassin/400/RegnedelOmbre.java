package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RegnedelOmbre extends ClassModel {
    public RegnedelOmbre() {
        super("regne_de_l_ombre", "Regne de l'Ombre", "Son regne est eternel. Les ombres sont sa couronne et les tenebres son royaume.",
                Rarity.LEGENDARY, ClassType.ASSASSIN, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.GOD_SLAYER_SWIFTNESS, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                174, 132, 56, 96, 378, 344);
    }
}
