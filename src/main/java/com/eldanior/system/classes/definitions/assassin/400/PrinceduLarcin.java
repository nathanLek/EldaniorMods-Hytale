package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PrinceduLarcin extends ClassModel {
    public PrinceduLarcin() {
        super("prince_du_larcin", "Prince du Larcin", "Le prince des voleurs, craint et respecte dans tous les bas-fonds du monde.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.KEEN_SENSES, PassiveSkill.LUCKY_STRIKE, PassiveSkill.PHANTOM_DODGE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                12, 8, 4, 4, 32, 28);
    }
}
