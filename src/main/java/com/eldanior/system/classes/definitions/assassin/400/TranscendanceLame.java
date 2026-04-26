package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TranscendanceLame extends ClassModel {
    public TranscendanceLame() {
        super("transcendance_lame", "Transcendance Lame", "Il a transcende l'art de la lame. Son epee est une extension de son ame.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                166, 72, 16, 72, 300, 174);
    }
}
