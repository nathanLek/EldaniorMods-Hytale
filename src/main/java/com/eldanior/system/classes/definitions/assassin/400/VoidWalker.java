package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VoidWalker extends ClassModel {
    public VoidWalker() {
        super("void_walker", "Void Walker", "Il marche entre les dimensions, insaisissable et invincible.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_STEP, PassiveSkill.DIMENSIONAL_STEP, PassiveSkill.VOID_BLADE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                80, 54, 20, 54, 166, 144);
    }
}
