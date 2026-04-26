package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbreAbsolue extends ClassModel {
    public OmbreAbsolue() {
        super("ombre_absolue", "Ombre Absolue", "L'ombre ultime. Elle est partout et nulle part a la fois.",
                Rarity.LEGENDARY, ClassType.ASSASSIN, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.DEMIGOD_SWIFTNESS, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                204, 102, 34, 102, 424, 306);
    }
}
