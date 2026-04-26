package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandAlchimiste extends ClassModel {
    public GrandAlchimiste() {
        super("grand_alchimiste", "Grand Alchimiste", "Le maitre alchimiste qui transmute la realite elle-meme.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.GOLDEN_TOUCH, PassiveSkill.GENIUS_MIND, PassiveSkill.DRAGON_BLOOD), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                10, 44, 78, 34, 26, 86);
    }
}
