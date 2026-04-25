package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Alchimiste extends ClassModel {
    public Alchimiste() {
        super("alchimiste", "Alchimiste", "L'Alchimiste transforme la matiere et cree des elixirs aux pouvoirs extraordinaires. Science et magie fusionnent.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.GOLDEN_TOUCH, PassiveSkill.EXPANDED_MIND, PassiveSkill.TROLL_BLOOD),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                6, 25, 45, 20, 15, 50);
    }
}
