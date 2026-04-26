package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SniperMystique extends ClassModel {
    public SniperMystique() {
        super("sniper_mystique", "Sniper Mystique", "Un tireur dont les fleches sont guidees par une force mystique.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.RAZOR_SENSES, PassiveSkill.SPELLBLADE, PassiveSkill.EXPANDED_MIND), List.of(WeaponMastery.BOW), List.of(), 400, false,
                30, 28, 20, 20, 82, 84);
    }
}
