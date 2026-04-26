package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FureurSanglante extends ClassModel {
    public FureurSanglante() {
        super("fureur_sanglante", "Fureur Sanglante", "La Fureur Sanglante se nourrit du sang verse sur le champ de bataille. Plus le combat dure, plus sa rage devient insatiable et meurtriere.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.FURY_STRIKE, PassiveSkill.COMBAT_VIGOR), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                48, 16, 3, 14, 19, 10);
    }
}