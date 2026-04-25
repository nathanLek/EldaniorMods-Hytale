package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Illusionniste extends ClassModel {
    public Illusionniste() {
        super("illusionniste", "Illusionniste", "L'Illusionniste trompe les sens de ses adversaires. Ses mirages et ses leurres sement la confusion totale.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.GALE_STEP, PassiveSkill.ARCANE_SHIELD),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                6, 20, 45, 15, 40, 35);
    }
}
