// 1. Le Titan (Divin - Indestructible)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Titan extends ClassModel {
    public Titan() {
        super("titan", "Titan", "Un etre primordial dont la peau est plus dure que le diamant. Le Titan est la terre elle-meme.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.GOD_CONSTITUTION, PassiveSkill.ETERNAL_LIFE, PassiveSkill.ETERNAL_FORTRESS), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                200, 300, 0, 140, 40, 20);
    }
}