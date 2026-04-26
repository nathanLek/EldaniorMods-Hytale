package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AcrobateVirtuose extends ClassModel {
    public AcrobateVirtuose() {
        super("acrobate_virtuose", "Acrobate Virtuose", "Un artiste martial dont les mouvements defient la gravite.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.GALE_STEP, PassiveSkill.ACROBATIC_POISE, PassiveSkill.THUNDER_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                10, 10, 4, 7, 42, 14);
    }
}
