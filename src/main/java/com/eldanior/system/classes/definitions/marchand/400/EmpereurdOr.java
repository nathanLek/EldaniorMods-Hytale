package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EmpereurdOr extends ClassModel {
    public EmpereurdOr() {
        super("empereur_d_or", "Empereur d'Or", "L'empereur dont la fortune est plus grande que tous les royaumes reunis.",
                Rarity.UNIQUE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.ANY), List.of(), 400, false,
                20, 20, 28, 20, 34, 156);
    }
}
