package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaindArgent extends ClassModel {
    public MaindArgent() {
        super("main_d_argent", "Main d'Argent", "Ses doigts de velours derobent avec une precision chirurgicale. Un artiste du vol.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.GOLDEN_TOUCH, PassiveSkill.RAZOR_SENSES, PassiveSkill.DUELIST_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                16, 6, 4, 2, 30, 26);
    }
}
