package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameDiamant extends ClassModel {
    public LameDiamant() {
        super("lame_diamant", "Lame Diamant", "Sa lame est aussi pure et tranchante que le diamant le plus parfait.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                48, 28, 10, 16, 90, 68);
    }
}
