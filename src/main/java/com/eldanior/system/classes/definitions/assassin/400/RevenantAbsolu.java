package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RevenantAbsolu extends ClassModel {
    public RevenantAbsolu() {
        super("revenant_absolu", "Revenant Absolu", "Un revenant que meme la mort ne peut retenir.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.VOID_STEP), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                132, 82, 24, 72, 268, 200);
    }
}
